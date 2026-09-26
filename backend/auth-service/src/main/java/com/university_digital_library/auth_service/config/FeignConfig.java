package com.university_digital_library.auth_service.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * auth-service gọi sang user-service (tạo profile khi admin đăng ký tài khoản
 * mới) là cuộc gọi service-to-service thuần túy, không có JWT của người dùng
 * cuối để forward — cần header X-Internal-Key để user-service tin tưởng
 * (xem InternalServiceAuthFilter bên user-service).
 */
@Configuration
public class FeignConfig {

    @Value("${security.internal.api-key}")
    private String internalApiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> requestTemplate.header("X-Internal-Key", internalApiKey);
    }
}
