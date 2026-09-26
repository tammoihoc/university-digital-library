package com.university_digital_library.auth_service.controller;

import com.university_digital_library.auth_service.dto.AuthRequest;
import com.university_digital_library.auth_service.dto.AuthResponse;
import com.university_digital_library.auth_service.dto.RegisterRequest;
import com.university_digital_library.auth_service.model.User;
import com.university_digital_library.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    // Lớp phòng thủ thứ 2 (SecurityConfig.hasRole("ADMIN") là lớp 1): dù filter
    // chain có bị cấu hình sai/gỡ nhầm sau này, @PreAuthorize ở đây vẫn chặn.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            return ResponseEntity.ok(AuthResponse.builder()
                .username(user.getUsername())
                .roles(user.getRoles())
                .message("User registered successfully")
                .build());
        } catch (Exception ex) {
            log.error("Registration error: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Login error: {}", ex.getMessage());
            return ResponseEntity.status(401).body(ex.getMessage());
        }
    }
    
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            AuthResponse response = authService.validateToken(token);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Token validation error: {}", ex.getMessage());
            return ResponseEntity.status(401).body(ex.getMessage());
        }
    }
    
    @GetMapping("/users/{username}")
    public ResponseEntity<?> getUserInfo(@PathVariable String username) {
        try {
            AuthResponse response = authService.getUserInfo(username);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Get user info error: {}", ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is healthy! 🔐");
    }
}
