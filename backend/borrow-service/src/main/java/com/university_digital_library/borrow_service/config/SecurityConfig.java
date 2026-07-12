// borrow-service/src/main/java/.../config/SecurityConfig.java
package com.university_digital_library.borrow_service.config;

import com.university_digital_library.borrow_service.security.JwtAuthFilter;
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
    
    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health", "/actuator/health", "/test").permitAll()
                // Chỉ ADMIN và LIBRARIAN mới được xem danh sách đặt lịch
                .requestMatchers("/reservations/active/all", "/reservations/all", "/reservations/book/**")
                    .hasAnyRole("ADMIN", "LIBRARIAN")
                // Sinh viên chỉ được xem đặt lịch của mình
                .requestMatchers("/reservations/my").authenticated()
                .requestMatchers("/reservations").authenticated()
                // Mượn trực tiếp chỉ dành cho ADMIN và LIBRARIAN
                .requestMatchers("/borrows/direct").hasAnyRole("ADMIN", "LIBRARIAN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
