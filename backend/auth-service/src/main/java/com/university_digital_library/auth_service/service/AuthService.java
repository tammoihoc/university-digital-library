package com.university_digital_library.auth_service.service;

import com.university_digital_library.auth_service.dto.AuthRequest;
import com.university_digital_library.auth_service.dto.AuthResponse;
import com.university_digital_library.auth_service.dto.CreateUserProfileRequest;
import com.university_digital_library.auth_service.dto.RegisterRequest;
import com.university_digital_library.auth_service.feign.UserProfileClient;
import com.university_digital_library.auth_service.model.User;
import com.university_digital_library.auth_service.repository.UserRepository;
import com.university_digital_library.common_library.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserProfileClient userProfileClient;

    // Giới hạn số lần đăng nhập sai để chống brute-force/dò mật khẩu.
    // Lưu in-memory là đủ cho quy mô đồ án; nếu chạy nhiều instance auth-service
    // song song thì nên thay bằng Redis để đếm dùng chung giữa các instance.
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000L; // 15 phút
    private final ConcurrentHashMap<String, AtomicInteger> failedAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lockoutUntil = new ConcurrentHashMap<>();
    
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        // Endpoint này giờ chỉ ADMIN gọi được (khóa ở SecurityConfig + @PreAuthorize
        // trên controller) nên có thể an toàn cho phép ADMIN chỉ định role — không
        // còn rủi ro tự leo thang đặc quyền như hồi endpoint còn permitAll công khai.
        // role đã được @Pattern validate ở DTO chỉ nhận đúng 4 giá trị cho phép,
        // nên ở đây chỉ cần fallback STUDENT khi admin bỏ trống.
        String requestedRole = (request.getRole() == null || request.getRole().isBlank())
            ? "STUDENT" : request.getRole();

        User user = User.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .roles(Set.of(requestedRole))
            .isActive(true)
            .build();
        
        User saved = userRepository.save(user);

        // Tạo profile tương ứng bên user-service. Nếu lỗi, để exception
        // propagate lên trên để @Transactional rollback user vừa tạo,
        // tránh tình trạng có user ở auth_db nhưng không có profile.
        CreateUserProfileRequest profileRequest = new CreateUserProfileRequest();
        profileRequest.setUsername(saved.getUsername());
        profileRequest.setFirstName(request.getFirstName());
        profileRequest.setLastName(request.getLastName());
        profileRequest.setEmail(request.getEmail());
        profileRequest.setPhone(request.getPhone());
        profileRequest.setFaculty(request.getFaculty());
        profileRequest.setMajor(request.getMajor());
        profileRequest.setDepartment(request.getDepartment());
        String userType = determineUserType(saved.getRoles());
        profileRequest.setUserType("UNKNOWN".equals(userType) ? "STUDENT" : userType);
        userProfileClient.createUserProfile(profileRequest);

        return saved;
    }
    
    @Transactional
    public AuthResponse login(AuthRequest request) {
        String username = request.getUsername();

        // Chặn ngay từ đầu nếu tài khoản đang bị khóa tạm, không tốn công
        // gọi AuthenticationManager (vốn tốn CPU vì BCrypt) — vừa nhanh vừa
        // tránh dò tiếp trong lúc bị khóa.
        Long lockedUntil = lockoutUntil.get(username);
        if (lockedUntil != null) {
            if (System.currentTimeMillis() < lockedUntil) {
                long remainingMinutes = (lockedUntil - System.currentTimeMillis()) / 60000 + 1;
                log.warn("Từ chối đăng nhập '{}': tài khoản đang bị khóa tạm, còn {} phút", username, remainingMinutes);
                throw new RuntimeException(
                    "Tài khoản tạm khóa do đăng nhập sai quá nhiều lần. Vui lòng thử lại sau " + remainingMinutes + " phút.");
            }
            // Hết thời gian khóa -> dọn dẹp, cho thử lại
            lockoutUntil.remove(username);
            failedAttempts.remove(username);
        }

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            registerFailedAttempt(username);
            throw new RuntimeException("Invalid username or password");
        }

        // Đăng nhập đúng -> reset bộ đếm lỗi của tài khoản này
        failedAttempts.remove(username);
        lockoutUntil.remove(username);

        User user = userRepository.findByUsernameAndIsActiveTrue(username)
            .orElseThrow(() -> new RuntimeException("User not found or inactive"));
        
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        String token = jwtService.generateToken(user.getUsername(), user.getRoles());
        
        return AuthResponse.builder()
            .token(token)
            .username(user.getUsername())
            .roles(user.getRoles())
            .userType(determineUserType(user.getRoles()))
            .message("Login successful")
            .build();
    }

    /**
     * Tăng bộ đếm đăng nhập sai cho username, và khóa tạm nếu chạm ngưỡng
     * MAX_FAILED_ATTEMPTS. Dùng AtomicInteger + computeIfAbsent để an toàn
     * khi nhiều request đăng nhập sai tới cùng lúc (tránh race condition
     * làm đếm thiếu hoặc khóa muộn).
     */
    private void registerFailedAttempt(String username) {
        int attempts = failedAttempts
            .computeIfAbsent(username, k -> new AtomicInteger(0))
            .incrementAndGet();

        log.warn("Đăng nhập sai cho '{}': lần thứ {}/{}", username, attempts, MAX_FAILED_ATTEMPTS);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            lockoutUntil.put(username, System.currentTimeMillis() + LOCKOUT_DURATION_MS);
            failedAttempts.remove(username);
            log.warn("Tài khoản '{}' bị khóa tạm {} phút do đăng nhập sai {} lần liên tiếp",
                username, LOCKOUT_DURATION_MS / 60000, MAX_FAILED_ATTEMPTS);
        }
    }
    
    public AuthResponse validateToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (!jwtService.validateToken(token)) {
            throw new RuntimeException("Invalid token");
        }
        
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return AuthResponse.builder()
            .username(user.getUsername())
            .roles(user.getRoles())
            .userType(determineUserType(user.getRoles()))
            .message("Token is valid")
            .build();
    }
    
    public AuthResponse getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return AuthResponse.builder()
            .username(user.getUsername())
            .roles(user.getRoles())
            .userType(determineUserType(user.getRoles()))
            .build();
    }
    
    private String determineUserType(Set<String> roles) {
        if (roles.contains("ADMIN")) return "ADMIN";
        if (roles.contains("LIBRARIAN")) return "LIBRARIAN";
        if (roles.contains("LECTURER")) return "LECTURER";
        if (roles.contains("STUDENT")) return "STUDENT";
        return "UNKNOWN";
    }
}
