package com.university_digital_library.user_service.model;

import com.university_digital_library.common_library.security.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    // 🔐 AES-256 ENCRYPTED FIELDS
    @Convert(converter = EncryptedStringConverter.class)
    private String firstName;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String lastName;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String email;

    // 🔎 BLIND INDEX: HMAC-SHA256(email) — không random như AES-GCM nên
    // dùng để tra cứu (findByEmailHash) và ràng buộc UNIQUE thật sự hoạt
    // động. Cột `email` phía trên chỉ dùng để lưu trữ/hiển thị, KHÔNG dùng
    // trong mệnh đề WHERE nữa vì mỗi lần mã hóa ra ciphertext khác nhau.
    @Column(name = "email_hash", unique = true, length = 64)
    private String emailHash;

    @Convert(converter = EncryptedStringConverter.class)
    private String phone;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String address;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String studentId;

    // 🔎 BLIND INDEX cho studentId — xem giải thích ở emailHash phía trên.
    @Column(name = "student_id_hash", unique = true, length = 64)
    private String studentIdHash;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String faculty;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String major;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String lecturerId;

    // 🔎 BLIND INDEX cho lecturerId — xem giải thích ở emailHash phía trên.
    @Column(name = "lecturer_id_hash", unique = true, length = 64)
    private String lecturerIdHash;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String department;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String librarianId;
    
    // Non-encrypted fields
    private String avatarUrl;
    private String coverPhotoUrl;
    
    @Enumerated(EnumType.STRING)
    private UserType userType;
    
    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    private LocalDate dateOfBirth;
    private String nationality;
    
    private Integer academicYear;
    private String academicTitle;
    private String shift;
    private String position;
    
    // Borrow management
    @Builder.Default
    private Integer currentBorrowed = 0;
    
    @Builder.Default
    private Integer totalBooksRead = 0;
    
    @Builder.Default
    private Integer maxBorrowLimit = 5;
    
    @Builder.Default
    private Boolean isLocked = false;
    
    private LocalDateTime lockedUntil;
    
    @Builder.Default
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastLoginAt;
    
    public enum UserType {
        STUDENT, LECTURER, LIBRARIAN, ADMIN
    }
    
    public enum Gender {
        MALE, FEMALE, OTHER
    }
    
    public String getFullName() {
        return (lastName != null ? lastName + " " : "") + (firstName != null ? firstName : "");
    }
    
    public boolean canBorrowMore() {
        return !isLocked && currentBorrowed < maxBorrowLimit;
    }
    
    public boolean isLocked() {
        if (isLocked && lockedUntil != null && lockedUntil.isBefore(LocalDateTime.now())) {
            // Auto-unlock if expired
            isLocked = false;
            lockedUntil = null;
        }
        return isLocked;
    }
}
