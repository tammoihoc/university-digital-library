package com.university_digital_library.user_service.config;

import com.university_digital_library.user_service.security.InternalServiceAuthFilter;
import com.university_digital_library.user_service.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private final JwtAuthFilter jwtAuthFilter;
    private final InternalServiceAuthFilter internalServiceAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, InternalServiceAuthFilter internalServiceAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.internalServiceAuthFilter = internalServiceAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/health",
                    "/users/health",
                    "/actuator/health",
                    "/users/student/*",
                    "/uploads/avatars/**",
                    "/users/*/avatar"
                ).permitAll()
                // ĐÃ SỬA LỖ HỔNG: trước đây permitAll() hoàn toàn trên cả 2 endpoint,
                // cho phép bất kỳ ai không cần đăng nhập gọi POST /users/{id}/borrow-count
                // để tự ý sửa số sách đang mượn của BẤT KỲ user nào (không có kiểm tra
                // quyền nào trong code cũ). borrow-count giờ CHỈ cho ADMIN/LIBRARIAN
                // hoặc service nội bộ (X-Internal-Key). borrow-info vẫn cho user tự xem
                // thông tin của chính mình (kiểm tra chi tiết trong controller), nhưng
                // bắt buộc phải đăng nhập/có internal key thay vì permitAll tuyệt đối.
                .requestMatchers("/users/*/borrow-count").hasAnyRole("ADMIN", "LIBRARIAN", "INTERNAL")
                .requestMatchers("/users/*/borrow-info").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(internalServiceAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
