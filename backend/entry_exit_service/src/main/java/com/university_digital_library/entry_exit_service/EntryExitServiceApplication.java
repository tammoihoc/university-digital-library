// entry-exit-service/src/main/java/com/university_digital_library/entry_exit_service/EntryExitServiceApplication.java
package com.university_digital_library.entry_exit_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan(basePackages = {
    "com.university_digital_library.entry_exit_service",
    "com.university_digital_library.common_library"  // ✅ THÊM DÒNG NÀY
})
public class EntryExitServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EntryExitServiceApplication.class, args);
    }
}
