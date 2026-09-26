// fine-service/src/main/java/.../model/DamageType.java
package com.university_digital_library.fine_service.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "penalties",
    indexes = {
        // Phủ findByUserId + findByUserIdAndIsActiveTrue.
        @Index(name = "idx_penalty_user_active", columnList = "user_id, is_active"),
        // Phủ findByIsActiveTrue khi không lọc theo user.
        @Index(name = "idx_penalty_active", columnList = "is_active")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Penalty {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PenaltyType penaltyType;
    
    private String reason;
    
    @Enumerated(EnumType.STRING)
    private PenaltyLevel level;
    
    private Integer newBorrowLimit;
    private LocalDateTime lockedUntil;
    
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private String createdBy;
    
    public enum PenaltyType {
        LATE_RETURN, DAMAGED, LOST, MULTIPLE_VIOLATIONS
    }
    
    public enum PenaltyLevel {
        WARNING, MINOR, MODERATE, SEVERE, PERMANENT
    }
}
