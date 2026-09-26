package com.university_digital_library.entry_exit_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "entry_exit_records",
    indexes = {
        // Cột nóng nhất: mọi check-in đều gọi findFirstByUserIdAndStatus...
        // / existsByUserIdAndStatusAndEntryTimeAfter (bao gồm cả logic chặn
        // check-in trùng). user_id + status là điều kiện bằng, entry_time
        // dùng để sort/range nên đặt cuối cùng — đúng thứ tự composite index.
        @Index(name = "idx_entryexit_user_status_time", columnList = "user_id, status, entry_time"),
        // Phủ findByStatus khi không lọc theo user (VD: đếm số người đang ở trong thư viện).
        @Index(name = "idx_entryexit_status", columnList = "status"),
        // Phủ findByEntryTimeBetween (báo cáo theo khoảng thời gian).
        @Index(name = "idx_entryexit_entry_time", columnList = "entry_time")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntryExitRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private LocalDateTime entryTime;
    
    private LocalDateTime exitTime;
    
    @Column(length = 10)
    private String branch;
    
    @Column(length = 20)
    @Builder.Default
    private String status = "INSIDE";
    
    private String userType;
    private String fullName;
    
    @Column(unique = false)   // ✅ bỏ UNIQUE để nhiều check-in
    private String studentId;
    
    private String faculty;
    private String major;
    
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @PrePersist
    protected void onCreate() {
        if (entryTime == null) entryTime = LocalDateTime.now();
        if (status == null) status = "INSIDE";
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
