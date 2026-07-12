package com.university_digital_library.user_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    // AVATAR - THÊM CÁC TRƯỜNG ẢNH
    private String avatarUrl;  // Ảnh đại diện chính
    private String coverPhotoUrl;  // Ảnh bìa hồ sơ
    
    @ElementCollection
    @CollectionTable(name = "user_gallery_images", joinColumns = @JoinColumn(name = "user_profile_id"))
    @Column(name = "image_url")
    private List<String> galleryImageUrls = new ArrayList<>();  // Ảnh trong gallery
    
    // Thông tin cá nhân
    private String firstName;
    private String lastName;
    
    @Column(unique = true)
    private String email;
    
    private String phone;
    private String address;
    
    @Enumerated(EnumType.STRING)
    private UserType userType;
    
    // Thông tin giới tính
    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    private LocalDate dateOfBirth;
    private String nationality;
    
    // Thông tin SINH VIÊN
    @Column(unique = true)
    private String studentId;
    
    private String faculty;
    private String major;
    private Integer academicYear;
    
    // Thông tin GIẢNG VIÊN
    private String lecturerId;
    private String department;
    private String academicTitle;
    
    // Thông tin THỦ THƯ
    private String librarianId;
    private String shift;
    private String position;
    
    // Quản lý mượn sách
    private Integer currentBorrowed = 0;
    
    private Integer totalBooksRead = 0;
    
    // Thông tin tài khoản
    private Boolean isActive = true;
    
    private LocalDateTime lastLoginAt;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public enum UserType {
        STUDENT, LECTURER, LIBRARIAN, ADMIN
    }
    
    public enum Gender {
        MALE, FEMALE, OTHER
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (currentBorrowed == null) currentBorrowed = 0;
        if (totalBooksRead == null) totalBooksRead = 0;
        if (galleryImageUrls == null) galleryImageUrls = new ArrayList<>();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Get full name
    public String getFullName() {
        return (lastName != null ? lastName + " " : "") + (firstName != null ? firstName : "");
    }
    
    // Generate default avatar based on gender and name
    public String getDefaultAvatar() {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            return avatarUrl;
        }
        
        // Tạo avatar mặc định dựa trên giới tính và tên
        String baseUrl = "https://api.dicebear.com/7.x/";
        String style = "avataaars";
        
        if (gender == Gender.FEMALE) {
            style = "avataaars";
        } else if (gender == Gender.MALE) {
            style = "avataaars";
        }
        
        return baseUrl + style + "/svg?seed=" + username + 
               "&backgroundColor=b6e3f4,c0aede,d1d4f9" +
               "&hairColor=2c1b18,4a312c,724133" +
               "&clothingColor=262e33,5199e4,65c9ff";
    }
}
