package com.example.huganstorev2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS - Cho phép tất cả các request từ mọi nguồn (Quan trọng cho Swagger và FE)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // CSRF - Tắt vì dùng JWT Stateless
            .csrf(csrf -> csrf.disable())
            // Session - Stateless (không lưu session)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            // Phân quyền request
            .authorizeHttpRequests(auth -> auth
                // Cho phép Swagger, Login, Register không cần token
                .requestMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/auth/register",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-ui/index.html",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/products",
                    "/api/v1/products/**",
                    "/api/v1/categories",
                    "/api/v1/categories/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/orders", "/api/v1/orders/").permitAll()
                // Tất cả các API khác đều cần xác thực
                .anyRequest().authenticated()
            )
            
            // Thêm JWT Filter trước UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ═══════════════════════════════════════════════════
    // CẤU HÌNH CORS - Cho phép FE và Swagger gọi API
    // ═══════════════════════════════════════════════════
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Cho phép tất cả domain (dùng trong dev)
        configuration.setAllowedMethods(List.of("*")); // Cho phép tất cả methods (GET, POST, PUT, DELETE, ...)
        configuration.setAllowedHeaders(List.of("*")); // Cho phép tất cả headers
        configuration.setAllowCredentials(false); // Không yêu cầu credentials

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}