// borrow-service/src/main/java/.../config/FeignConfig.java
package com.university_digital_library.borrow_service.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
@Slf4j
public class FeignConfig {

    // Dùng để xác thực các cuộc gọi service-to-service (vd: returnBook cập nhật
    // borrow-count nhưng không có JWT người dùng để forward vì được gọi từ
    // @Scheduled/nghiệp vụ nội bộ) — xem InternalServiceAuthFilter bên user-service.
    @Value("${security.internal.api-key}")
    private String internalApiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Luôn gắn internal key để các endpoint service-to-service (vd:
            // /users/*/borrow-count) xác thực được ngay cả khi không có Authorization
            // header của người dùng (ví dụ trong returnBook() và scheduled job
            // autoExpireReservations() không có JWT nào để forward).
            requestTemplate.header("X-Internal-Key", internalApiKey);

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authorization = request.getHeader("Authorization");
                if (authorization != null && !authorization.isEmpty()) {
                    requestTemplate.header("Authorization", authorization);
                    log.debug("Feign - Added Authorization header");
                }
            }
        };
    }
}
