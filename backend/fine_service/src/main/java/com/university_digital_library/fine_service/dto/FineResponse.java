// fine-service/src/main/java/com/university_digital_library/fine_service/dto/FineResponse.java
package com.university_digital_library.fine_service.dto;

import com.university_digital_library.fine_service.model.Fine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FineResponse {
    private Long id;
    private String userId;
    private Long borrowId;
    private Double amount;
    private String penaltyType;
    private String penaltyTypeLabel;
    private String reason;
    private Boolean isPaid;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private String createdBy;
    private String damageType;
    private String damageTypeLabel;
    private Double bookPrice;
    private String damageDescription;
    private String message;
    // trong FineResponse.java
public static FineResponse fromDTO(FineDTO dto) {
    if (dto == null) return null;
    return FineResponse.builder()
        .id(dto.getId())
        .userId(dto.getUserId())
        .borrowId(dto.getBorrowId())
        .amount(dto.getAmount())
        .penaltyType(dto.getPenaltyType())
        .penaltyTypeLabel(dto.getPenaltyTypeLabel())
        .reason(dto.getReason())
        .isPaid(dto.getIsPaid())
        .paidAt(dto.getPaidAt())
        .createdAt(dto.getCreatedAt())
        .createdBy(dto.getCreatedBy())
        .damageType(dto.getDamageType())
        .damageTypeLabel(dto.getDamageTypeLabel())
        .bookPrice(dto.getBookPrice())
        .damageDescription(dto.getDamageDescription())
        .build();
}
    public static FineResponse fromEntity(Fine fine) {
        if (fine == null) return null;
        
        return FineResponse.builder()
            .id(fine.getId())
            .userId(fine.getUserId())
            .borrowId(fine.getBorrowId())
            .amount(fine.getAmount())
            .penaltyType(fine.getPenaltyType() != null ? fine.getPenaltyType().name() : null)
            .penaltyTypeLabel(getPenaltyTypeLabel(fine.getPenaltyType()))
            .reason(fine.getReason())
            .isPaid(fine.getIsPaid())
            .paidAt(fine.getPaidAt())
            .createdAt(fine.getCreatedAt())
            .createdBy(fine.getCreatedBy())
            .damageType(fine.getDamageType() != null ? fine.getDamageType().name() : null)
            .damageTypeLabel(fine.getDamageType() != null ? fine.getDamageType().getDescription() : null)
            .bookPrice(fine.getBookPrice())
            .damageDescription(fine.getDamageDescription())
            .build();
    }
    
    private static String getPenaltyTypeLabel(Fine.PenaltyType type) {
        if (type == null) return "Khác";
        switch (type) {
            case MONEY: return "Phạt tiền";
            case COMMUNITY_SERVICE: return "Lao động công ích";
            case BAN_TEMPORARY: return "Cấm mượn tạm thời";
            case WARNING: return "Cảnh cáo";
            default: return "Khác";
        }
    }
}
