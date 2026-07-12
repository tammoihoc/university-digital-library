// /home/tam/university-digital-library/backend/fine-service/src/main/java/com/university_digital_library/fine_service/FineServiceApplication.java
package com.university_digital_library.fine_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FineServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(FineServiceApplication.class, args);
        System.out.println("✅ Fine Service started on port 8086");
    }
}
