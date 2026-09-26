// fine-service/src/main/java/com/university_digital_library/fine_service/model/Fine.java
package com.university_digital_library.fine_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "fine_records",
    indexes = {
        // Phủ findByUserId + findByUserIdAndIsPaidFalse (user_id là cột đầu).
        @Index(name = "idx_fine_user_paid", columnList = "user_id, is_paid"),
        // Phủ findByIsPaidFalse/True, countByIsPaidFalse, sumUnpaidFines, và
        // findByCreatedAtBeforeAndIsPaidFalse (is_paid = điều kiện bằng nên
        // đặt trước created_at là điều kiện khoảng, đúng nguyên tắc composite index).
        @Index(name = "idx_fine_unpaid_created", columnList = "is_paid, created_at"),
        // Phủ findByCreatedAtBetween khi không lọc theo is_paid.
        @Index(name = "idx_fine_created_at", columnList = "created_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fine {  // ✅ ĐỔI TÊN CLASS THÀNH Fine (không phải FineRecord)
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    private Long borrowId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PenaltyType penaltyType;
    
    @Column(nullable = false)
    private Double amount;
    
    private String reason;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isPaid = false;
    
    private LocalDateTime paidAt;
    
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private String createdBy;
    
    // Damage fields
    @Enumerated(EnumType.STRING)
    private DamageType damageType;
    private Double bookPrice;
    private String damageDescription;
    
    public enum PenaltyType {
        MONEY, COMMUNITY_SERVICE, BAN_TEMPORARY, WARNING
    }
    
    public enum DamageType {
        LOST("Mất sách"),
        DAMAGED_HEAVY("Hư hỏng nặng"),
        DAMAGED_LIGHT("Hư hỏng nhẹ");
        
        private final String description;
        DamageType(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}
