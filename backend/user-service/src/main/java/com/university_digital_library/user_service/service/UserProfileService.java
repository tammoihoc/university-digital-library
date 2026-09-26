package com.university_digital_library.user_service.service;

import com.university_digital_library.common_library.security.BlindIndexService;
import com.university_digital_library.user_service.dto.CreateUserProfileRequest;
import com.university_digital_library.user_service.dto.UpdateBorrowLimitRequest;
import com.university_digital_library.user_service.dto.UpdateUserProfileRequest;
import com.university_digital_library.user_service.dto.UserProfileDTO;
import com.university_digital_library.user_service.model.UserProfile;
import com.university_digital_library.user_service.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserProfileRepository repository;
    private final IdGeneratorService idGeneratorService;
    private final BlindIndexService blindIndexService;   // ✅ HMAC blind index cho email/studentId/lecturerId
    private final Path avatarStorageLocation = Paths.get("uploads/avatars").toAbsolutePath().normalize();

    @Transactional
    public UserProfileDTO createUserProfile(CreateUserProfileRequest request) {
        // Check if exists
        if (repository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        String emailHash = blindIndexService.hash(request.getEmail());
        if (emailHash != null && repository.existsByEmailHash(emailHash)) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        // Auto-generate ID based on user type
        String generatedId = null;
        if (request.getUserType() == UserProfile.UserType.STUDENT) {
            if (request.getStudentId() == null || request.getStudentId().isEmpty()) {
                generatedId = idGeneratorService.generateId(UserProfile.UserType.STUDENT);
                request.setStudentId(generatedId);
            }
        } else if (request.getUserType() == UserProfile.UserType.LECTURER) {
            if (request.getLecturerId() == null || request.getLecturerId().isEmpty()) {
                generatedId = idGeneratorService.generateId(UserProfile.UserType.LECTURER);
                request.setLecturerId(generatedId);
            }
        } else if (request.getUserType() == UserProfile.UserType.LIBRARIAN) {
            if (request.getLibrarianId() == null || request.getLibrarianId().isEmpty()) {
                generatedId = idGeneratorService.generateId(UserProfile.UserType.LIBRARIAN);
                request.setLibrarianId(generatedId);
            }
        }

        // Tính blind index cho studentId/lecturerId sau khi đã auto-generate (nếu có)
        // để đảm bảo kiểm tra trùng và tra cứu sau này đều dựa trên giá trị cuối cùng.
        String studentIdHash = blindIndexService.hash(request.getStudentId());
        if (studentIdHash != null && repository.findByStudentIdHash(studentIdHash).isPresent()) {
            throw new IllegalArgumentException("Student ID already exists: " + request.getStudentId());
        }
        String lecturerIdHash = blindIndexService.hash(request.getLecturerId());
        if (lecturerIdHash != null && repository.findByLecturerIdHash(lecturerIdHash).isPresent()) {
            throw new IllegalArgumentException("Lecturer ID already exists: " + request.getLecturerId());
        }

        UserProfile profile = UserProfile.builder()
            .username(request.getUsername())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .emailHash(emailHash)
            .phone(request.getPhone())
            .address(request.getAddress())
            .userType(request.getUserType())
            .gender(request.getGender())
            .dateOfBirth(request.getDateOfBirth())
            .studentId(request.getStudentId())
            .studentIdHash(studentIdHash)
            .faculty(request.getFaculty())
            .major(request.getMajor())
            .academicYear(request.getAcademicYear())
            .lecturerId(request.getLecturerId())
            .lecturerIdHash(lecturerIdHash)
            .department(request.getDepartment())
            .academicTitle(request.getAcademicTitle())
            .librarianId(request.getLibrarianId())
            .shift(request.getShift())
            .position(request.getPosition())
            .currentBorrowed(0)
            .maxBorrowLimit(5)
            .isLocked(false)
            .isActive(true)
            .build();

        UserProfile saved = repository.save(profile);
        log.info("Created user profile: {} with ID: {}", saved.getUsername(),
                 generatedId != null ? generatedId : "No auto ID");

        return UserProfileDTO.fromEntity(saved);
    }

    public UserProfileDTO getUserProfile(String username) {
        UserProfile profile = repository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return UserProfileDTO.fromEntity(profile);
    }

    public UserProfile getUserProfileEntity(String username) {
        return repository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    // ✅ SỬA: dùng blind index (email_hash/student_id_hash) thay vì quét toàn
    // bộ bảng rồi tự giải mã từng dòng để so sánh. Cách cũ vừa SAI (không xử
    // lý được các bản ghi cũ lưu ciphertext khác IV) vừa CHẬM O(n) — càng
    // nhiều user càng chậm vì phải decrypt AES cho từng người một. Cách mới
    // tra thẳng bằng index trên cột hash, O(1) theo DB index.
    public UserProfile getUserByStudentId(String studentId) {
        String hash = blindIndexService.hash(studentId);
        if (hash == null) return null;
        return repository.findByStudentIdHash(hash).orElse(null);
    }


    @Transactional
    public UserProfileDTO updateUserProfile(String username, UpdateUserProfileRequest request) {
        UserProfile profile = getUserProfileEntity(username);

        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getEmail() != null) {
            String newEmailHash = blindIndexService.hash(request.getEmail());
            // Check if email is used by another user — tra bằng blind index,
            // không dùng cột email đã mã hóa (xem giải thích ở repository).
            repository.findByEmailHash(newEmailHash)
                .ifPresent(existing -> {
                    if (!existing.getUsername().equals(username)) {
                        throw new IllegalArgumentException("Email already exists: " + request.getEmail());
                    }
                });
            profile.setEmail(request.getEmail());
            profile.setEmailHash(newEmailHash);   // giữ đồng bộ hash mỗi khi email đổi
        }
        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());
        if (request.getGender() != null) profile.setGender(request.getGender());
        if (request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());

        // Update student info
        if (profile.getUserType() == UserProfile.UserType.STUDENT) {
            if (request.getFaculty() != null) profile.setFaculty(request.getFaculty());
            if (request.getMajor() != null) profile.setMajor(request.getMajor());
            if (request.getAcademicYear() != null) profile.setAcademicYear(request.getAcademicYear());
        }

        // Update lecturer info
        if (profile.getUserType() == UserProfile.UserType.LECTURER) {
            if (request.getDepartment() != null) profile.setDepartment(request.getDepartment());
            if (request.getAcademicTitle() != null) profile.setAcademicTitle(request.getAcademicTitle());
        }

        UserProfile updated = repository.save(profile);
        return UserProfileDTO.fromEntity(updated);
    }

@Transactional
public UserProfileDTO updateBorrowCount(String username, Integer newCount) {
    log.info("📝 Updating borrow count for user {} to {}", username, newCount);
    UserProfile profile = getUserProfileEntity(username);
    profile.setCurrentBorrowed(newCount);
    UserProfile saved = repository.save(profile);
    log.info("✅ Updated borrow count for user {} to {}", username, saved.getCurrentBorrowed());
    return UserProfileDTO.fromEntity(saved);
}

    @Transactional
    public UserProfileDTO updateBorrowLimit(String username, UpdateBorrowLimitRequest request) {
        UserProfile profile = getUserProfileEntity(username);

        if (request.getMaxBorrowLimit() != null) {
            profile.setMaxBorrowLimit(request.getMaxBorrowLimit());
        }
        if (request.getIsLocked() != null) {
            profile.setIsLocked(request.getIsLocked());
        }
        if (request.getLockedUntil() != null) {
            profile.setLockedUntil(request.getLockedUntil());
        }

        UserProfile saved = repository.save(profile);
        log.info("Updated borrow limit for user {}: max={}, locked={}",
                 username, saved.getMaxBorrowLimit(), saved.getIsLocked());

        return UserProfileDTO.fromEntity(saved);
    }

    @Transactional
    public UserProfileDTO lockUser(String username, Integer days, String reason) {
        UserProfile profile = getUserProfileEntity(username);

        profile.setIsLocked(true);
        if (days != null && days > 0) {
            profile.setLockedUntil(LocalDateTime.now().plusDays(days));
        }

        UserProfile saved = repository.save(profile);
        log.info("Locked user {} for {} days. Reason: {}", username, days, reason);

        return UserProfileDTO.fromEntity(saved);
    }

    @Transactional
    public UserProfileDTO unlockUser(String username) {
        UserProfile profile = getUserProfileEntity(username);

        profile.setIsLocked(false);
        profile.setLockedUntil(null);

        UserProfile saved = repository.save(profile);
        log.info("Unlocked user {}", username);

        return UserProfileDTO.fromEntity(saved);
    }

    public List<UserProfileDTO> getAllUsers() {
        return repository.findAll().stream()
            .map(UserProfileDTO::fromEntity)
            .collect(Collectors.toList());
    }

    public List<UserProfileDTO> getStudents() {
        return repository.findByUserType(UserProfile.UserType.STUDENT).stream()
            .map(UserProfileDTO::fromEntity)
            .collect(Collectors.toList());
    }

    public List<UserProfileDTO> getUsersByType(UserProfile.UserType userType) {
        return repository.findByUserType(userType).stream()
            .map(UserProfileDTO::fromEntity)
            .collect(Collectors.toList());
    }

    public List<UserProfileDTO> searchUsers(String keyword) {
        return repository.searchUsers(keyword).stream()
            .map(UserProfileDTO::fromEntity)
            .collect(Collectors.toList());
    }

    public String uploadAvatar(String username, MultipartFile file) {
        try {
            UserProfile profile = getUserProfileEntity(username);

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Only image files are allowed");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";

            String newFilename = "avatar_" + username + "_" + System.currentTimeMillis() + extension;

            Files.createDirectories(avatarStorageLocation);
            Path filePath = avatarStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String avatarUrl = "/uploads/avatars/" + newFilename;
            profile.setAvatarUrl(avatarUrl);
            repository.save(profile);

            return avatarUrl;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage());
        }
    }

    public byte[] getAvatarBytes(String username) {
        UserProfile profile = getUserProfileEntity(username);
        String avatarUrl = profile.getAvatarUrl();

        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return null;
        }

        try {
            // Lấy filename từ URL (ví dụ: /uploads/avatars/avatar_admin_123456.jpg)
            String filename;
            if (avatarUrl.startsWith("/uploads/avatars/")) {
                filename = avatarUrl.substring("/uploads/avatars/".length());
            } else if (avatarUrl.contains("/")) {
                filename = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);
            } else {
                filename = avatarUrl;
            }

            Path filePath = avatarStorageLocation.resolve(filename);
            if (!Files.exists(filePath)) {
                return null;
            }

            return Files.readAllBytes(filePath);

        } catch (IOException e) {
            log.error("Error reading avatar file: {}", e.getMessage());
            return null;
        }
    }
}
