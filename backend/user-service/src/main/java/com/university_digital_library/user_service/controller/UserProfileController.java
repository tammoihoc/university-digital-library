// user-service/src/main/java/.../controller/UserProfileController.java
package com.university_digital_library.user_service.controller;

import com.university_digital_library.user_service.dto.CreateUserProfileRequest;
import com.university_digital_library.user_service.dto.UpdateUserProfileRequest;
import com.university_digital_library.user_service.dto.UserProfileDTO;
import com.university_digital_library.user_service.model.UserProfile;
import com.university_digital_library.user_service.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    
    // ========== PUBLIC API (Không cần token) ==========
    
    @PostMapping("/profile")
    public ResponseEntity<?> createUserProfile(@RequestBody CreateUserProfileRequest request) {
        try {
            UserProfileDTO profile = userProfileService.createUserProfile(request);
            return ResponseEntity.ok(profile);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
    
    // ========== API CẦN TOKEN ==========
    
// user-service/src/main/java/.../controller/UserProfileController.java
// Sửa API getUserProfile

// user-service/src/main/java/.../controller/UserProfileController.java
@GetMapping("/profile/{username}")
public ResponseEntity<?> getUserProfile(@PathVariable String username, Authentication auth) {
    try {
        if (auth == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        String currentUser = auth.getName();
        boolean isAdminOrLibrarian = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                              a.getAuthority().equals("ROLE_LIBRARIAN"));
        
        // ✅ CHO PHÉP ADMIN/LIBRARIAN XEM TẤT CẢ
        if (isAdminOrLibrarian) {
            try {
                UserProfileDTO profile = userProfileService.getUserProfile(username);
                return ResponseEntity.ok(profile);
            } catch (Exception e) {
                // Nếu không tìm thấy profile, trả về thông tin cơ bản
                Map<String, Object> basicInfo = new HashMap<>();
                basicInfo.put("username", username);
                basicInfo.put("fullName", username);
                basicInfo.put("userType", "STUDENT");
                basicInfo.put("studentId", "N/A");
                basicInfo.put("faculty", "N/A");
                basicInfo.put("major", "N/A");
                return ResponseEntity.ok(basicInfo);
            }
        }
        
        // STUDENT: chỉ xem được chính mình
        if (!currentUser.equals(username)) {
            return ResponseEntity.status(403).body("You can only view your own profile");
        }
        
        UserProfileDTO profile = userProfileService.getUserProfile(username);
        return ResponseEntity.ok(profile);
        
    } catch (Exception ex) {
        Map<String, Object> basicInfo = new HashMap<>();
        basicInfo.put("username", username);
        basicInfo.put("fullName", username);
        basicInfo.put("userType", "STUDENT");
        basicInfo.put("studentId", "N/A");
        basicInfo.put("faculty", "N/A");
        basicInfo.put("major", "N/A");
        return ResponseEntity.ok(basicInfo);
    }
}
    
    @PutMapping("/profile/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable String username,
            @RequestBody UpdateUserProfileRequest request,
            Authentication auth) {
        try {
            // Kiểm tra quyền: chỉ admin/librarian hoặc chính user mới được sửa
            String currentUser = auth.getName();
            boolean isAdminOrLibrarian = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                                  a.getAuthority().equals("ROLE_LIBRARIAN"));
            
            if (!isAdminOrLibrarian && !currentUser.equals(username)) {
                return ResponseEntity.status(403).body("You can only update your own profile");
            }
            
            UserProfileDTO updatedProfile = userProfileService.updateUserProfile(username, request);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<UserProfileDTO>> getAllUsers() {
        List<UserProfileDTO> users = userProfileService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<UserProfileDTO>> getAllStudents() {
        List<UserProfileDTO> students = userProfileService.getStudents();
        return ResponseEntity.ok(students);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<UserProfileDTO>> searchStudents(@RequestParam String q) {
        List<UserProfileDTO> students = userProfileService.searchStudents(q);
        return ResponseEntity.ok(students);
    }
    
    @GetMapping("/type/{userType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<UserProfileDTO>> getUsersByType(@PathVariable UserProfile.UserType userType) {
        List<UserProfileDTO> users = userProfileService.getUsersByType(userType);
        return ResponseEntity.ok(users);
    }
    
    // ========== API CHO BORROW SERVICE (Chỉ ADMIN/LIBRARIAN) ==========
    
@GetMapping("/{userId}/borrow-info")
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
public ResponseEntity<Map<String, Object>> getUserBorrowInfo(
        @PathVariable String userId,
        Authentication auth) {
    
    // ✅ LOG ĐỂ DEBUG
    log.info("User: {}, Roles: {}", auth.getName(), auth.getAuthorities());
    
    UserProfile profile = userProfileService.getUserProfileEntity(userId);
    
    Map<String, Object> response = new HashMap<>();
    response.put("userId", profile.getUsername());
    response.put("maxBorrowLimit", 5);
    response.put("currentBorrowed", profile.getCurrentBorrowed() != null ? profile.getCurrentBorrowed() : 0);
    response.put("canBorrowMore", profile.getCurrentBorrowed() == null || profile.getCurrentBorrowed() < 5);
    
    return ResponseEntity.ok(response);
}
    
    // ========== API CHO SINH VIÊN/GIẢNG VIÊN (Xem thông tin của chính mình) ==========
    
    @GetMapping("/my/borrow-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMyBorrowInfo(Authentication auth) {
        String userId = auth.getName();
        UserProfile profile = userProfileService.getUserProfileEntity(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", profile.getUsername());
        response.put("maxBorrowLimit", 5);
        response.put("currentBorrowed", profile.getCurrentBorrowed() != null ? profile.getCurrentBorrowed() : 0);
        response.put("canBorrowMore", profile.getCurrentBorrowed() == null || profile.getCurrentBorrowed() < 5);
        
        log.info("User {} checked their borrow info", userId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{userId}/borrow-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<?> updateBorrowCount(
            @PathVariable String userId,
            @RequestParam Integer newCount) {
        UserProfile profile = userProfileService.getUserProfileEntity(userId);
        
        profile.setCurrentBorrowed(newCount);
        userProfileService.saveUserProfile(profile);
        
        log.info("Updated borrow count for user {} to {}", userId, newCount);
        return ResponseEntity.ok("Borrow count updated");
    }
    
    // =========== AVATAR APIs ===========
    
    @PostMapping("/{username}/avatar/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadAvatar(
            @PathVariable String username,
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        try {
            // Kiểm tra quyền: chỉ admin/librarian hoặc chính user mới được upload avatar
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
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
    
    @GetMapping("/{username}/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String username, Authentication auth) {
        // Kiểm tra quyền: chỉ admin/librarian hoặc chính user mới được xem avatar
        String currentUser = auth.getName();
        boolean isAdminOrLibrarian = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                              a.getAuthority().equals("ROLE_LIBRARIAN"));
        
        if (!isAdminOrLibrarian && !currentUser.equals(username)) {
            return ResponseEntity.status(403).body(null);
        }
        return userProfileService.getUserAvatar(username);
    }
    
    @DeleteMapping("/{username}/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteAvatar(
            @PathVariable String username,
            Authentication auth) {
        try {
            // Kiểm tra quyền: chỉ admin/librarian hoặc chính user mới được xóa avatar
            String currentUser = auth.getName();
            boolean isAdminOrLibrarian = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                                  a.getAuthority().equals("ROLE_LIBRARIAN"));
            
            if (!isAdminOrLibrarian && !currentUser.equals(username)) {
                return ResponseEntity.status(403).body("You can only delete your own avatar");
            }
            
            UserProfile user = userProfileService.getUserProfileEntity(username);
            user.setAvatarUrl(null);
            userProfileService.saveUserProfile(user);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Avatar deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
