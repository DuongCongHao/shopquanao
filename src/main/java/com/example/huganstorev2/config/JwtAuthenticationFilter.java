package com.example.huganstorev2.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import com.example.huganstorev2.models.user.entity.User;
import com.example.huganstorev2.models.user.repository.UserRepository;
import com.example.huganstorev2.models.user.service.UserService;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    // BỎ QUA CÁC URL KHÔNG CẦN XÁC THỰC
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Cho phép khách đặt hàng không cần đăng nhập
        if ("POST".equalsIgnoreCase(request.getMethod())
                && "/api/v1/orders".equals(path)) {
            return true;
        }

        // Cho phép xem sản phẩm và danh mục không cần đăng nhập
        if ("GET".equalsIgnoreCase(request.getMethod())
                && (
                    path.equals("/api/v1/products")
                    || path.startsWith("/api/v1/products/")
                    || path.equals("/api/v1/categories")
                    || path.startsWith("/api/v1/categories/")
                )) {
            return true;
        }

        // Các URL public khác
        return path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-ui.html")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars")
                || path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/auth/register");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Lấy token từ Authorization header
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // 2. Kiểm tra JWT
        if (jwtService.isTokenValid(token)) {

            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);
            String sessionId = jwtService.extractSessionId(token);

            // Token không có sessionId
            if (sessionId == null || sessionId.isBlank()) {
                sendUnauthorized(
                        response,
                        "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại."
                );
                return;
            }

            LocalDateTime expiredBefore =
                    LocalDateTime.now().minusMinutes(
                            UserService.SESSION_TIMEOUT_MINUTES
                    );

            // 3. Kiểm tra xem session trong token có còn là session hiện tại không
            boolean currentSessionActive =
                    userRepository
                            .existsByEmailAndActiveSessionIdAndSessionLastSeenAtGreaterThanEqual(
                                    email,
                                    sessionId,
                                    expiredBefore
                            );

            if (!currentSessionActive) {

                /*
                 * Không inject UserService ở đây.
                 *
                 * Làm vậy tránh vòng lặp:
                 *
                 * JwtAuthenticationFilter
                 *        -> UserService
                 *        -> PasswordEncoder / SecurityConfig
                 *        -> JwtAuthenticationFilter
                 */

                Optional<User> userOptional = userRepository.findByEmail(email);

                if (userOptional.isPresent()) {
                    User user = userOptional.get();

                    String activeSessionId = user.getActiveSessionId();
                    LocalDateTime lastSeenAt = user.getSessionLastSeenAt();

                    /*
                     * Nếu DB đang có một sessionId KHÁC với sessionId
                     * trong JWT này và session đó vẫn còn hoạt động,
                     * nghĩa là tài khoản đã được đăng nhập ở nơi khác.
                     */
                    boolean replacedByAnotherSession =
                            activeSessionId != null
                            && !activeSessionId.isBlank()
                            && !activeSessionId.equals(sessionId)
                            && lastSeenAt != null
                            && !lastSeenAt.isBefore(expiredBefore);

                    if (replacedByAnotherSession) {
                        sendUnauthorized(
                                response,
                                "Tài khoản đang được đăng nhập ở nơi khác. Phiên này đã bị đăng xuất."
                        );
                        return;
                    }
                }

                // Không có session mới thay thế -> phiên cũ đã hết hạn
                sendUnauthorized(
                        response,
                        "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
                );
                return;
            }

            // 4. Tạo quyền
            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(role);

            List<SimpleGrantedAuthority> authorities =
                    List.of(authority);

            // 5. Tạo Authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            authorities
                    );

            // Lưu sessionId để heartbeat/logout sử dụng
            authentication.setDetails(sessionId);

            // 6. Đưa Authentication vào SecurityContext
            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
        }

        // 7. Cho request đi tiếp
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(
            HttpServletResponse response,
            String message
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}