// fine-service/src/main/java/com/university_digital_library/fine_service/dto/PenaltyResponse.java
package com.university_digital_library.fine_service.dto;

import com.university_digital_library.fine_service.model.Penalty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PenaltyResponse {
    private Long id;
    private String userId;
    private String penaltyType;
    private String penaltyTypeLabel;
    private String reason;
    private String level;
    private String levelLabel;
    private Integer newBorrowLimit;
    private LocalDateTime lockedUntil;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy;
    private String message;
    
    public static PenaltyResponse fromEntity(Penalty penalty) {
        if (penalty == null) return null;
        
        return PenaltyResponse.builder()
            .id(penalty.getId())
            .userId(penalty.getUserId())
            .penaltyType(penalty.getPenaltyType() != null ? penalty.getPenaltyType().name() : null)
            .penaltyTypeLabel(getPenaltyTypeLabel(penalty.getPenaltyType()))
            .reason(penalty.getReason())
            .level(penalty.getLevel() != null ? penalty.getLevel().name() : null)
            .levelLabel(penalty.getLevel() != null ? getLevelLabel(penalty.getLevel()) : null)
            .newBorrowLimit(penalty.getNewBorrowLimit())
            .lockedUntil(penalty.getLockedUntil())
            .isActive(penalty.getIsActive())
            .createdAt(penalty.getCreatedAt())
            .createdBy(penalty.getCreatedBy())
            .build();
    }
    
    private static String getPenaltyTypeLabel(Penalty.PenaltyType type) {
        if (type == null) return "Khác";
        switch (type) {
            case LATE_RETURN: return "Trả sách trễ";
            case DAMAGED: return "Hư hỏng sách";
            case LOST: return "Mất sách";
            case MULTIPLE_VIOLATIONS: return "Nhiều vi phạm";
            default: return "Khác";
        }
    }
    
    private static String getLevelLabel(Penalty.PenaltyLevel level) {
        if (level == null) return "Khác";
        switch (level) {
            case WARNING: return "Cảnh cáo";
            case MINOR: return "Nhẹ";
            case MODERATE: return "Trung bình";
            case SEVERE: return "Nặng";
            case PERMANENT: return "Vĩnh viễn";
            default: return "Khác";
        }
    }
}
