package com.university_digital_library.user_service.controller;

import com.university_digital_library.user_service.dto.CreateUserProfileRequest;
import com.university_digital_library.user_service.dto.UpdateBorrowLimitRequest;
import com.university_digital_library.user_service.dto.UpdateUserProfileRequest;
import com.university_digital_library.user_service.dto.UserProfileDTO;
import com.university_digital_library.user_service.model.UserProfile;
import com.university_digital_library.user_service.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserProfileService userProfileService;

    // ========== ADMIN-ONLY / INTERNAL ==========
    // ĐÃ ĐỔI: trước đây permitAll() công khai hoàn toàn, ai cũng POST được tạo
    // profile bừa bãi. Giờ chỉ ADMIN (thao tác trực tiếp) hoặc INTERNAL (auth-service
    // gọi Feign kèm X-Internal-Key khi admin đăng ký tài khoản qua /auth/register)
    // mới gọi được — khớp với thay đổi /auth/register giờ cũng chỉ ADMIN gọi được.
    @PostMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'INTERNAL')")
    public ResponseEntity<?> createUserProfile(@Valid @RequestBody CreateUserProfileRequest request) {
        try {
            UserProfileDTO profile = userProfileService.createUserProfile(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(profile);
        } catch (Exception ex) {
            log.error("Create profile error: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // ========== PUBLIC ENDPOINTS ==========

    @GetMapping("/student/{studentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserByStudentId(@PathVariable String studentId) {
        try {
            UserProfile profile = userProfileService.getUserByStudentId(studentId);
            if (profile == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(UserProfileDTO.fromEntity(profile));
        } catch (Exception ex) {
            log.error("Get user by studentId error: {}", ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ========== PROTECTED ENDPOINTS ==========

    @GetMapping("/profile/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserProfile(@PathVariable String username, Authentication auth) {
        try {
            String currentUser = auth.getName();
            boolean isAdminOrLibrarian = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                              a.getAuthority().equals("ROLE_LIBRARIAN"));

            if (!isAdminOrLibrarian && !currentUser.equals(username)) {
                return ResponseEntity.status(403).body("You can only view your own profile");
            }

            UserProfileDTO profile = userProfileService.getUserProfile(username);
            return ResponseEntity.ok(profile);

        } catch (Exception ex) {
            log.error("Get profile error: {}", ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyProfile(Authentication auth) {
        String username = auth.getName();
        return getUserProfile(username, auth);
    }

    @PutMapping("/profile/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable String username,
            @Valid @RequestBody UpdateUserProfileRequest request,
            Authentication auth) {

        try {
            String currentUser = auth.getName();
            boolean isAdminOrLibrarian = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                              a.getAuthority().equals("ROLE_LIBRARIAN"));

            if (!isAdminOrLibrarian && !currentUser.equals(username)) {
                return ResponseEntity.status(403).body("You can only update your own profile");
            }

            UserProfileDTO profile = userProfileService.updateUserProfile(username, request);
            return ResponseEntity.ok(profile);

        } catch (Exception ex) {
            log.error("Update profile error: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // ========== ADMIN/LIBRARIAN ENDPOINTS ==========

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<UserProfileDTO>> getAllUsers() {
        return ResponseEntity.ok(userProfileService.getAllUsers());
    }

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<UserProfileDTO>> getAllStudents() {
        return ResponseEntity.ok(userProfileService.getStudents());
    }

    @GetMapping("/type/{userType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<UserProfileDTO>> getUsersByType(
            @PathVariable UserProfile.UserType userType) {
        return ResponseEntity.ok(userProfileService.getUsersByType(userType));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<UserProfileDTO>> searchUsers(@RequestParam String q) {
        return ResponseEntity.ok(userProfileService.searchUsers(q));
    }

    // ========== BORROW INFO FOR BORROW SERVICE ==========
@GetMapping("/{userId}/borrow-info")
public ResponseEntity<Map<String, Object>> getUserBorrowInfo(
        @PathVariable String userId,
        Authentication auth) {
    // Cho phép user tự xem thông tin của mình, ADMIN/LIBRARIAN xem bất kỳ,
    // hoặc service nội bộ (ROLE_INTERNAL, gán bởi InternalServiceAuthFilter khi
    // có header X-Internal-Key hợp lệ — dùng khi borrow-service cần tra cứu
    // hạn mức mượn của user mà không có sẵn JWT của người dùng để forward).
    String currentUser = auth.getName();
    boolean isPrivileged = auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                       a.getAuthority().equals("ROLE_LIBRARIAN") ||
                       a.getAuthority().equals("ROLE_INTERNAL"));
    if (!isPrivileged && !currentUser.equals(userId)) {
        throw new RuntimeException("You can only view your own borrow info");
    }
    UserProfileDTO profile = userProfileService.getUserProfile(userId);
    Map<String, Object> response = new HashMap<>();
    response.put("userId", profile.getUsername());
    response.put("maxBorrowLimit", profile.getMaxBorrowLimit());
    response.put("currentBorrowed", profile.getCurrentBorrowed());
    response.put("canBorrowMore", profile.getCurrentBorrowed() < profile.getMaxBorrowLimit());
    response.put("isLocked", profile.getIsLocked());
    response.put("lockedUntil", profile.getLockedUntil());
    response.put("fullName", profile.getFullName());
    response.put("userType", profile.getUserType());
    return ResponseEntity.ok(response);
}

    @PostMapping("/{userId}/borrow-count")
    // Khôi phục @PreAuthorize — cùng với SecurityConfig.hasAnyRole("ADMIN","LIBRARIAN","INTERNAL")
    // ở tầng filter chain, đây là lớp bảo vệ thứ 2 (defense in depth) chống lại lỗ hổng
    // cho phép sửa borrow-count của bất kỳ ai mà không cần xác thực.
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'INTERNAL')")
    public ResponseEntity<?> updateBorrowCount(
            @PathVariable String userId,
            @RequestParam Integer newCount) {
        log.info("📝 Updating borrow count for user {} to {}", userId, newCount);
        UserProfileDTO profile = userProfileService.updateBorrowCount(userId, newCount);
        return ResponseEntity.ok(profile);
    }

    // ========== BORROW LIMIT MANAGEMENT (ADMIN ONLY) ==========

    @PutMapping("/{userId}/borrow-limit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBorrowLimit(
            @PathVariable String userId,
            @Valid @RequestBody UpdateBorrowLimitRequest request) {

        UserProfileDTO profile = userProfileService.updateBorrowLimit(userId, request);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/{userId}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> lockUser(
            @PathVariable String userId,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) String reason) {

        UserProfileDTO profile = userProfileService.lockUser(userId, days, reason);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/{userId}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unlockUser(@PathVariable String userId) {
        UserProfileDTO profile = userProfileService.unlockUser(userId);
        return ResponseEntity.ok(profile);
    }

    // ========== AVATAR ==========

    @PostMapping("/{username}/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadAvatar(
            @PathVariable String username,
            @RequestParam("file") MultipartFile file,
            Authentication auth) {

        try {
            String currentUser = auth.getName();
            boolean isAdminOrLibrarian = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                              a.getAuthority().equals("ROLE_LIBRARIAN"));

            if (!isAdminOrLibrarian && !currentUser.equals(username)) {
                return ResponseEntity.status(403).body("You can only upload avatar for yourself");
            }

            String avatarUrl = userProfileService.uploadAvatar(username, file);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Avatar uploaded successfully");
            response.put("avatarUrl", avatarUrl);
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Upload avatar error: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/{username}/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> getAvatar(
            @PathVariable String username,
            Authentication auth) {

        try {
            String currentUser = auth.getName();
            boolean isAdminOrLibrarian = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                              a.getAuthority().equals("ROLE_LIBRARIAN"));

            if (!isAdminOrLibrarian && !currentUser.equals(username)) {
                return ResponseEntity.status(403).build();
            }

            byte[] avatarBytes = userProfileService.getAvatarBytes(username);
            if (avatarBytes == null) {
                return ResponseEntity.notFound().build();
            }

            String contentType = "image/jpeg";

            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header("Cache-Control", "public, max-age=3600")
                    .body(avatarBytes);

        } catch (Exception ex) {
            log.error("Get avatar error: {}", ex.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
