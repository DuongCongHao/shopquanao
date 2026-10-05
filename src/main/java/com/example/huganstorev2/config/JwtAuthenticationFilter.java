package com.example.huganstorev2.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.example.huganstorev2.models.user.repository.UserRepository;
import com.example.huganstorev2.models.user.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;

    // BỎ QUA CÁC URL KHÔNG CẦN XÁC THỰC
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/api/v1/orders".equals(path)) {
            return true;
        }
        if ("GET".equalsIgnoreCase(request.getMethod()) &&
                (path.equals("/api/v1/products") ||
                 path.startsWith("/api/v1/products/") ||
                 path.equals("/api/v1/categories") ||
                 path.startsWith("/api/v1/categories/"))) {
            return true;
        }
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/swagger-ui.html") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-resources") ||
               path.startsWith("/webjars") ||
               path.equals("/api/v1/auth/login") ||
               path.equals("/api/v1/auth/register");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Lấy token từ Header "Authorization"
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // 2. Kiểm tra token hợp lệ
        if (jwtService.isTokenValid(token)) {
            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);
            String sessionId = jwtService.extractSessionId(token);

            if (sessionId == null || !userRepository.existsByEmailAndActiveSessionIdAndSessionLastSeenAtGreaterThanEqual(
                    email,
                    sessionId,
                    java.time.LocalDateTime.now().minusMinutes(UserService.SESSION_TIMEOUT_MINUTES)
            )) {
                String message = sessionId != null && userService.hasReplacementSession(email, sessionId)
                    ? "Tài khoản đang được đăng nhập ở nơi khác. Phiên này đã bị đăng xuất."
                    : "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write(message);
                return;
            }

            // 3. Tạo danh sách quyền (authorities)
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
            List<SimpleGrantedAuthority> authorities = List.of(authority);

            // 4. Tạo đối tượng Authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
            authentication.setDetails(sessionId);

            // 5. Đặt vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 6. Cho request đi tiếp
        filterChain.doFilter(request, response);
    }
}