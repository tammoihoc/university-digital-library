// user-service/src/main/java/com/university_digital_library/user_service/UserServiceApplication.java
package com.university_digital_library.user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;  // ✅ THÊM IMPORT

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing  // ✅ THÊM DÒNG NÀY ĐỂ ENABLE JPA AUDITING
@ComponentScan(basePackages = {
    "com.university_digital_library.user_service",
    "com.university_digital_library.common_library"
})
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
