// user-service/src/main/java/.../service/UserProfileService.java
package com.university_digital_library.user_service.service;

import com.university_digital_library.user_service.dto.CreateUserProfileRequest;
import com.university_digital_library.user_service.dto.UpdateUserProfileRequest;
import com.university_digital_library.user_service.dto.UserProfileDTO;
import com.university_digital_library.user_service.model.UserProfile;
import com.university_digital_library.user_service.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j  // ✅ THÊM ANNOTATION NÀY
public class UserProfileService {
    
    private final UserProfileRepository userProfileRepository;
    private final IdGeneratorService idGeneratorService;
    
    // =========== CREATE PROFILE WITH AUTO-GENERATED ID ===========
    
    public UserProfileDTO createUserProfile(CreateUserProfileRequest request) {
        if (userProfileRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("User profile already exists for username: " + request.getUsername());
        }
        
        // TỰ ĐỘNG TẠO MÃ SỐ DỰA VÀO USER TYPE
        String generatedId = null;
        
        if (request.getUserType() == UserProfile.UserType.STUDENT) {
            generatedId = idGeneratorService.generateId(UserProfile.UserType.STUDENT);
            request.setStudentId(generatedId);
            log.info("Auto-generated Student ID: {} for user: {}", generatedId, request.getUsername());
        } 
        else if (request.getUserType() == UserProfile.UserType.LECTURER) {
            generatedId = idGeneratorService.generateId(UserProfile.UserType.LECTURER);
            request.setLecturerId(generatedId);
            log.info("Auto-generated Lecturer ID: {} for user: {}", generatedId, request.getUsername());
        }
        else if (request.getUserType() == UserProfile.UserType.LIBRARIAN) {
            generatedId = idGeneratorService.generateId(UserProfile.UserType.LIBRARIAN);
            request.setLibrarianId(generatedId);
            log.info("Auto-generated Librarian ID: {} for user: {}", generatedId, request.getUsername());
        }
        
        // Validate (bỏ qua kiểm tra ID vì đã tự tạo)
        validateUserProfile(request);
        
        UserProfile profile = new UserProfile();
        profile.setUsername(request.getUsername());
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setEmail(request.getEmail());
        profile.setPhone(request.getPhone());
        profile.setAddress(request.getAddress());
        profile.setUserType(request.getUserType());
        profile.setGender(request.getGender());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setStudentId(request.getStudentId());
        profile.setFaculty(request.getFaculty());
        profile.setMajor(request.getMajor());
        profile.setAcademicYear(request.getAcademicYear());
        profile.setLecturerId(request.getLecturerId());
        profile.setDepartment(request.getDepartment());
        profile.setAcademicTitle(request.getAcademicTitle());
        profile.setLibrarianId(request.getLibrarianId());
        profile.setShift(request.getShift());
        profile.setPosition(request.getPosition());
        profile.setCurrentBorrowed(0);
        profile.setIsActive(true);
        
        UserProfile saved = userProfileRepository.save(profile);
        log.info("Created user profile: {} with ID: {}", saved.getUsername(), 
                 generatedId != null ? generatedId : "No auto ID");
        
        return UserProfileDTO.fromEntity(saved);
    }
    
    // SỬA LẠI VALIDATE (bỏ kiểm tra ID vì đã tự tạo)
    private void validateUserProfile(CreateUserProfileRequest request) {
        // Kiểm tra email trùng
        if (request.getEmail() != null && userProfileRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }
        
        // Kiểm tra studentId (nếu có và không phải auto-generated)
        if (request.getUserType() == UserProfile.UserType.STUDENT && request.getStudentId() != null) {
            if (userProfileRepository.existsByStudentId(request.getStudentId())) {
                throw new IllegalArgumentException("Student ID already exists: " + request.getStudentId());
            }
        }
        
        // Kiểm tra lecturerId (nếu có)
        if (request.getUserType() == UserProfile.UserType.LECTURER && request.getLecturerId() != null) {
            if (userProfileRepository.existsByLecturerId(request.getLecturerId())) {
                throw new IllegalArgumentException("Lecturer ID already exists: " + request.getLecturerId());
            }
        }
    }
    
    // =========== UPDATE PROFILE (PUT) ===========
    
    @Transactional
    public UserProfileDTO updateUserProfile(String username, UpdateUserProfileRequest request) {
        UserProfile profile = getUserProfileEntity(username);
        
        // Cập nhật thông tin cá nhân
        if (request.getFirstName() != null) {
            profile.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            profile.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            // Kiểm tra email trùng (trừ email của chính user này)
            userProfileRepository.findByEmail(request.getEmail())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getUsername().equals(username)) {
                            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
                        }
                    });
            profile.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        
        // Cập nhật thông tin theo loại user
        if (profile.getUserType() == UserProfile.UserType.STUDENT) {
            updateStudentInfo(profile, request);
        } else if (profile.getUserType() == UserProfile.UserType.LECTURER) {
            updateLecturerInfo(profile, request);
        } else if (profile.getUserType() == UserProfile.UserType.LIBRARIAN) {
            updateLibrarianInfo(profile, request);
        }
        
        // Lưu và trả về
        UserProfile updated = userProfileRepository.save(profile);
        log.info("Updated user profile: {}", username);
        return UserProfileDTO.fromEntity(updated);
    }
    
    private void updateStudentInfo(UserProfile profile, UpdateUserProfileRequest request) {
        if (request.getStudentId() != null) {
            // Kiểm tra studentId trùng (trừ của chính user này)
            userProfileRepository.findByStudentId(request.getStudentId())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getUsername().equals(profile.getUsername())) {
                            throw new IllegalArgumentException("Student ID already exists: " + request.getStudentId());
                        }
                    });
            profile.setStudentId(request.getStudentId());
        }
        if (request.getFaculty() != null) {
            profile.setFaculty(request.getFaculty());
        }
        if (request.getMajor() != null) {
            profile.setMajor(request.getMajor());
        }
        if (request.getAcademicYear() != null) {
            profile.setAcademicYear(request.getAcademicYear());
        }
    }
    
    private void updateLecturerInfo(UserProfile profile, UpdateUserProfileRequest request) {
        if (request.getLecturerId() != null) {
            // Kiểm tra lecturerId trùng (trừ của chính user này)
            userProfileRepository.findByLecturerId(request.getLecturerId())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getUsername().equals(profile.getUsername())) {
                            throw new IllegalArgumentException("Lecturer ID already exists: " + request.getLecturerId());
                        }
                    });
            profile.setLecturerId(request.getLecturerId());
        }
        if (request.getDepartment() != null) {
            profile.setDepartment(request.getDepartment());
        }
        if (request.getAcademicTitle() != null) {
            profile.setAcademicTitle(request.getAcademicTitle());
        }
    }
    
    private void updateLibrarianInfo(UserProfile profile, UpdateUserProfileRequest request) {
        if (request.getLibrarianId() != null) {
            profile.setLibrarianId(request.getLibrarianId());
        }
        if (request.getShift() != null) {
            profile.setShift(request.getShift());
        }
        if (request.getPosition() != null) {
            profile.setPosition(request.getPosition());
        }
    }
    
    // =========== GET PROFILE ===========
    
    public UserProfileDTO getUserProfile(String username) {
        UserProfile profile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found: " + username));
        return UserProfileDTO.fromEntity(profile);
    }
    
    public UserProfile getUserProfileEntity(String username) {
        return userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found: " + username));
    }
    
    @Transactional
    public UserProfile saveUserProfile(UserProfile profile) {
        return userProfileRepository.save(profile);
    }
    
    // =========== OTHER METHODS ===========
    
    public List<UserProfileDTO> getAllUsers() {
        return userProfileRepository.findAll().stream()
                .map(UserProfileDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<UserProfileDTO> getStudents() {
        return userProfileRepository.findAll().stream()
                .filter(profile -> profile.getUserType() == UserProfile.UserType.STUDENT)
                .map(UserProfileDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<UserProfileDTO> getUsersByType(UserProfile.UserType userType) {
        return userProfileRepository.findAll().stream()
                .filter(profile -> profile.getUserType() == userType)
                .map(UserProfileDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<UserProfileDTO> searchStudents(String keyword) {
        return userProfileRepository.findAll().stream()
                .filter(profile -> profile.getUserType() == UserProfile.UserType.STUDENT)
                .filter(profile -> 
                    (profile.getStudentId() != null && profile.getStudentId().contains(keyword)) ||
                    (profile.getFullName() != null && profile.getFullName().toLowerCase().contains(keyword.toLowerCase())) ||
                    (profile.getFaculty() != null && profile.getFaculty().toLowerCase().contains(keyword.toLowerCase())) ||
                    (profile.getMajor() != null && profile.getMajor().toLowerCase().contains(keyword.toLowerCase()))
                )
                .map(UserProfileDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    // =========== AVATAR METHODS ===========
    
    public String uploadAvatar(String username, MultipartFile file) {
        try {
            // 1. Tìm user
            UserProfile user = getUserProfileEntity(username);
            
            // 2. Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            
            // 3. Kiểm tra định dạng
            String contentType = file.getContentType();
            if (contentType == null || 
                !(contentType.equals("image/jpeg") || 
                  contentType.equals("image/png") || 
                  contentType.equals("image/jpg") || 
                  contentType.equals("image/webp"))) {
                throw new IllegalArgumentException("Only JPEG, PNG, JPG, or WEBP images are allowed");
            }
            
            // 4. Tạo filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
            
            String newFilename = "avatar_" + username + "_" + System.currentTimeMillis() + extension;
            
            // 5. Tạo thư mục
            Path uploadsDir = Paths.get("uploads/avatars").toAbsolutePath().normalize();
            Files.createDirectories(uploadsDir);
            
            // 6. Lưu file
            Path filePath = uploadsDir.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 7. Cập nhật database
            String avatarUrl = "/uploads/avatars/" + newFilename;
            user.setAvatarUrl(avatarUrl);
            userProfileRepository.save(user);
            
            log.info("Uploaded avatar for user: {}", username);
            return avatarUrl;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage());
        }
    }
    
    public ResponseEntity<byte[]> getUserAvatar(String username) {
        try {
            UserProfile user = getUserProfileEntity(username);
            String avatarUrl = user.getAvatarUrl();
            
            if (avatarUrl == null || avatarUrl.isEmpty()) {
                // Trả về avatar mặc định nếu không có
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Avatar not found".getBytes());
            }
            
            // Lấy filename từ URL
            String filename;
            if (avatarUrl.startsWith("/uploads/avatars/")) {
                filename = avatarUrl.substring("/uploads/avatars/".length());
            } else if (avatarUrl.contains("/")) {
                filename = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);
            } else {
                filename = avatarUrl;
            }
            
            // Đọc file
            Path filePath = Paths.get("uploads/avatars", filename).toAbsolutePath();
            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Avatar file not found".getBytes());
            }
            
            byte[] imageBytes = Files.readAllBytes(filePath);
            
            // Xác định Content-Type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                // Dựa vào extension nếu không detect được
                if (filename.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                } else if (filename.toLowerCase().endsWith(".jpg") || 
                          filename.toLowerCase().endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (filename.toLowerCase().endsWith(".webp")) {
                    contentType = "image/webp";
                } else {
                    contentType = "application/octet-stream";
                }
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("Cache-Control", "public, max-age=3600")
                    .body(imageBytes);
                    
        } catch (Exception e) {
            log.error("Error getting avatar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }
}
