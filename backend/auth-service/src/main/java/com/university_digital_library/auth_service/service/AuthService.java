package com.university_digital_library.auth_service.service;

import com.university_digital_library.auth_service.dto.RegisterRequest;
import com.university_digital_library.auth_service.model.User;
import com.university_digital_library.auth_service.repository.UserRepository;
import com.university_digital_library.auth_service.feign.UserProfileClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileClient userProfileClient;

    public User register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Validate roles
        if (req.getRoles() == null || req.getRoles().isEmpty()) {
            throw new IllegalArgumentException("Roles are required");
        }
        
        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(req.getRoles() == null ? new HashSet<>() : req.getRoles())
                .build();
        
        // Validate roles before saving
        user.validateRoles();
        
        User savedUser = userRepository.save(user);
        
        // Gọi user-service để tạo user profile
        try {
            // Set username cho userProfile trước khi gửi
            req.getUserProfile().setUsername(savedUser.getUsername());
            userProfileClient.createUserProfile(req.getUserProfile());
        } catch (Exception e) {
            // Nếu tạo profile thất bại, xóa user đã tạo
            userRepository.delete(savedUser);
            throw new RuntimeException("Failed to create user profile: " + e.getMessage());
        }
        
        return savedUser;
    }
}
