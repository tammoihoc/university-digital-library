// /home/tam/university-digital-library/backend/fine-service/src/main/java/com/university_digital_library/fine_service/dto/CreateFineRequest.java
package com.university_digital_library.fine_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateFineRequest {
    @NotBlank(message = "User ID is required")
    private String userId;
    
    private Long borrowId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;
    
    private String penaltyType;
    private String reason;
    
    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Long getBorrowId() { return borrowId; }
    public void setBorrowId(Long borrowId) { this.borrowId = borrowId; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public String getPenaltyType() { return penaltyType; }
    public void setPenaltyType(String penaltyType) { this.penaltyType = penaltyType; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
