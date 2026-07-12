package com.university_digital_library.user_service.dto;

import com.university_digital_library.user_service.model.UserProfile;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate; // THÊM IMPORT NÀY

@Data
public class UserProfileDTO {
    private Long id;
    private String username;
    
    // THÊM AVATAR URL
    private String avatarUrl;
    private String coverPhotoUrl;
    
    // Thông tin cá nhân
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    
    private UserProfile.UserType userType;
    private UserProfile.Gender gender;
    private LocalDate dateOfBirth; // SỬA TỪ LocalDateTime → LocalDate
    
    // Thông tin sinh viên
    private String studentId;
    private String faculty;
    private String major;
    private Integer academicYear;
    
    // Thông tin giảng viên
    private String lecturerId;
    private String department;
    private String academicTitle;
    
    // Thông tin thủ thư
    private String librarianId;
    private String shift;
    private String position;
    
    // 🔥 THÊM FIELD MỚI
    private Integer currentBorrowed;
    
    private LocalDateTime createdAt;
    
    public static UserProfileDTO fromEntity(UserProfile profile) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(profile.getId());
        dto.setUsername(profile.getUsername());
        dto.setAvatarUrl(profile.getAvatarUrl());
        dto.setCoverPhotoUrl(profile.getCoverPhotoUrl());
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        dto.setFullName(profile.getFullName());
        dto.setEmail(profile.getEmail());
        dto.setPhone(profile.getPhone());
        dto.setAddress(profile.getAddress());
        dto.setUserType(profile.getUserType());
        dto.setGender(profile.getGender());
        dto.setDateOfBirth(profile.getDateOfBirth()); // BÂY GIỜ SẼ KHÔNG LỖI
        dto.setStudentId(profile.getStudentId());
        dto.setFaculty(profile.getFaculty());
        dto.setMajor(profile.getMajor());
        dto.setAcademicYear(profile.getAcademicYear());
        dto.setLecturerId(profile.getLecturerId());
        dto.setDepartment(profile.getDepartment());
        dto.setAcademicTitle(profile.getAcademicTitle());
        dto.setLibrarianId(profile.getLibrarianId());
        dto.setShift(profile.getShift());
        dto.setPosition(profile.getPosition());
        dto.setCurrentBorrowed(profile.getCurrentBorrowed());
        dto.setCreatedAt(profile.getCreatedAt());
        return dto;
    }
}
