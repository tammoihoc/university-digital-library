package com.university_digital_library.gateway_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;  // THÊM IMPORT NÀY
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {
    
    @GetMapping("/fallback/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Auth Service is temporarily unavailable. Please try again later.");
        response.put("path", "/api/auth/**");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    @GetMapping("/fallback/user")
    public ResponseEntity<Map<String, Object>> userFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "User Service is temporarily unavailable. Please try again later.");
        response.put("path", "/api/users/**");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    @GetMapping("/fallback/book")
    public ResponseEntity<Map<String, Object>> bookFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Book Service is temporarily unavailable. Please try again later.");
        response.put("path", "/api/books/**");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    @GetMapping("/fallback/borrow")
    public ResponseEntity<Map<String, Object>> borrowFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Borrow Service is temporarily unavailable. Please try again later.");
        response.put("path", "/api/borrows/**");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    @PostMapping("/fallback/borrow")
    public ResponseEntity<Map<String, Object>> borrowFallbackPost() {
        return borrowFallback(); // Gọi lại cùng method
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Gateway Service is healthy! 🚪");
    }
    
    @GetMapping("/actuator/health")
    public ResponseEntity<String> actuatorHealth() {
        return ResponseEntity.ok("{\"status\":\"UP\"}");
    }
    @GetMapping("/fallback/avatar")
public ResponseEntity<Map<String, Object>> avatarFallback() {
    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", System.currentTimeMillis());
    response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
    response.put("error", "Service Unavailable");
    response.put("message", "Avatar Service is temporarily unavailable. Please try again later.");
    response.put("path", "/uploads/avatars/**");
    
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
}
@GetMapping("/fallback/entry-exit")
public ResponseEntity<Map<String, Object>> entryExitFallback() {
    Map<String, Object> response = new HashMap<>();
    response.put("status", 503);
    response.put("error", "Service Unavailable");
    response.put("message", "Entry-Exit Service is temporarily unavailable");
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
}

@GetMapping("/fallback/fine")
public ResponseEntity<Map<String, Object>> fineFallback() {
    Map<String, Object> response = new HashMap<>();
    response.put("status", 503);
    response.put("error", "Service Unavailable");
    response.put("message", "Fine Service is temporarily unavailable");
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
}
}
