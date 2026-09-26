package com.university_digital_library.user_service.dto;

import com.university_digital_library.user_service.model.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String avatarUrl;
    private String coverPhotoUrl;
    
    private UserProfile.UserType userType;
    private UserProfile.Gender gender;
    private LocalDate dateOfBirth;
    
    private String studentId;
    private String faculty;
    private String major;
    private Integer academicYear;
    
    private String lecturerId;
    private String department;
    private String academicTitle;
    
    private String librarianId;
    private String shift;
    private String position;
    
    private Integer currentBorrowed;
    private Integer maxBorrowLimit;
    private Boolean isLocked;
    private LocalDateTime lockedUntil;
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static UserProfileDTO fromEntity(UserProfile profile) {
        return UserProfileDTO.builder()
            .id(profile.getId())
            .username(profile.getUsername())
            .firstName(profile.getFirstName())
            .lastName(profile.getLastName())
            .fullName(profile.getFullName())
            .email(profile.getEmail())
            .phone(profile.getPhone())
            .address(profile.getAddress())
            .avatarUrl(profile.getAvatarUrl())
            .coverPhotoUrl(profile.getCoverPhotoUrl())
            .userType(profile.getUserType())
            .gender(profile.getGender())
            .dateOfBirth(profile.getDateOfBirth())
            .studentId(profile.getStudentId())
            .faculty(profile.getFaculty())
            .major(profile.getMajor())
            .academicYear(profile.getAcademicYear())
            .lecturerId(profile.getLecturerId())
            .department(profile.getDepartment())
            .academicTitle(profile.getAcademicTitle())
            .librarianId(profile.getLibrarianId())
            .shift(profile.getShift())
            .position(profile.getPosition())
            .currentBorrowed(profile.getCurrentBorrowed())
            .maxBorrowLimit(profile.getMaxBorrowLimit())
            .isLocked(profile.isLocked())
            .lockedUntil(profile.getLockedUntil())
            .isActive(profile.getIsActive())
            .createdAt(profile.getCreatedAt())
            .updatedAt(profile.getUpdatedAt())
            .build();
    }
}
