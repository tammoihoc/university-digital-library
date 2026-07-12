// entry-exit-service/src/main/java/.../EntryExitServiceApplication.java
package com.university_digital_library.entry_exit_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients  // ✅ THÊM DÒNG NÀY
public class EntryExitServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(EntryExitServiceApplication.class, args);
        System.out.println("✅ Entry-Exit Service started on port 8085");
    }
}
