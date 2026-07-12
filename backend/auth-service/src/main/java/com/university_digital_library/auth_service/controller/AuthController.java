package com.university_digital_library.auth_service.controller;

import com.university_digital_library.auth_service.dto.AuthRequest;
import com.university_digital_library.auth_service.dto.AuthResponse;
import com.university_digital_library.auth_service.dto.RegisterRequest;
import com.university_digital_library.auth_service.model.User;
import com.university_digital_library.auth_service.repository.UserRepository;
import com.university_digital_library.auth_service.security.JwtUtil;
import com.university_digital_library.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            // Validate request
            req.validate();
            
            User u = authService.register(req);
            return ResponseEntity.ok(u.getUsername());
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body("Invalid username/password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(req.getUsername());
        var roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(java.util.stream.Collectors.toSet());
        String token = jwtUtil.generateToken(userDetails.getUsername(), roles);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // 🔐 THÊM CÁC ENDPOINT PROTECTED ĐỂ TEST TOKEN
    @GetMapping("/test-protected")
    public ResponseEntity<?> testProtected() {
        return ResponseEntity.ok("This is a protected endpoint! Access granted.");
    }

    @GetMapping("/admin-only")
    public ResponseEntity<?> adminOnly() {
        return ResponseEntity.ok("This is ADMIN only endpoint!");
    }

    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo() {
        return ResponseEntity.ok("User information endpoint");
    }
}
