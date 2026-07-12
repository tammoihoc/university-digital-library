// /home/tam/university-digital-library/backend/fine-service/src/main/java/com/university_digital_library/fine_service/dto/FineDTO.java
package com.university_digital_library.fine_service.dto;

import com.university_digital_library.fine_service.model.Fine;
import java.time.LocalDateTime;

public class FineDTO {
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
    
    public FineDTO() {}
    
    public static FineDTO fromEntity(Fine fine) {
        FineDTO dto = new FineDTO();
        dto.setId(fine.getId());
        dto.setUserId(fine.getUserId());
        dto.setBorrowId(fine.getBorrowId());
        dto.setAmount(fine.getAmount());
        dto.setPenaltyType(fine.getPenaltyType().name());
        dto.setPenaltyTypeLabel(getPenaltyTypeLabel(fine.getPenaltyType()));
        dto.setReason(fine.getReason());
        dto.setIsPaid(fine.getIsPaid());
        dto.setPaidAt(fine.getPaidAt());
        dto.setCreatedAt(fine.getCreatedAt());
        dto.setCreatedBy(fine.getCreatedBy());
                if (fine.getDamageType() != null) {
            dto.setDamageType(fine.getDamageType().name());
            dto.setDamageTypeLabel(fine.getDamageType().getDescription());
        }
        dto.setBookPrice(fine.getBookPrice());
        dto.setDamageDescription(fine.getDamageDescription());
        return dto;
    }
    
    private static String getPenaltyTypeLabel(Fine.PenaltyType type) {
        switch (type) {
            case MONEY: return "Phạt tiền";
            case COMMUNITY_SERVICE: return "Lao động công ích";
            case BAN_TEMPORARY: return "Cấm mượn tạm thời";
            case WARNING: return "Cảnh cáo";
            default: return "Khác";
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Long getBorrowId() { return borrowId; }
    public void setBorrowId(Long borrowId) { this.borrowId = borrowId; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public String getPenaltyType() { return penaltyType; }
    public void setPenaltyType(String penaltyType) { this.penaltyType = penaltyType; }
    
    public String getPenaltyTypeLabel() { return penaltyTypeLabel; }
    public void setPenaltyTypeLabel(String penaltyTypeLabel) { this.penaltyTypeLabel = penaltyTypeLabel; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public Boolean getIsPaid() { return isPaid; }
    public void setIsPaid(Boolean isPaid) { this.isPaid = isPaid; }
    
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        private String damageType;        // LOST, DAMAGED_HEAVY, DAMAGED_LIGHT
    private String damageTypeLabel;   // Mất sách, Hư hỏng nặng, Hư hỏng nhẹ
    private Double bookPrice;         // Giá gốc sách
    private String damageDescription; // Mô tả hư hỏng
    

    
    // Getters and Setters
    public String getDamageType() { return damageType; }
    public void setDamageType(String damageType) { this.damageType = damageType; }
    
    public String getDamageTypeLabel() { return damageTypeLabel; }
    public void setDamageTypeLabel(String damageTypeLabel) { this.damageTypeLabel = damageTypeLabel; }
    
    public Double getBookPrice() { return bookPrice; }
    public void setBookPrice(Double bookPrice) { this.bookPrice = bookPrice; }
    
    public String getDamageDescription() { return damageDescription; }
    public void setDamageDescription(String damageDescription) { this.damageDescription = damageDescription; }

}

