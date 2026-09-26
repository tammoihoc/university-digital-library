// borrow-service/src/main/java/com/university_digital_library/borrow_service/BorrowServiceApplication.java
package com.university_digital_library.borrow_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling; // ✅ THÊM

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling  // ⏰ BẬT SCHEDULING
@ComponentScan(basePackages = {
    "com.university_digital_library.borrow_service",
    "com.university_digital_library.common_library"
})
public class BorrowServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BorrowServiceApplication.class, args);
    }
}
