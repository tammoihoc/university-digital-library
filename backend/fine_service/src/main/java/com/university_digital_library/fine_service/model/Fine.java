// /home/tam/university-digital-library/backend/fine-service/src/main/java/com/university_digital_library/fine_service/model/Fine.java
package com.university_digital_library.fine_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fines")
public class Fine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    private Long borrowId;
    
    @Column(nullable = false)
    private Double amount;
    
    @Enumerated(EnumType.STRING)
    private PenaltyType penaltyType;
    
    private String reason;
    
    private Boolean isPaid;
    
    private LocalDateTime paidAt;
    
    private LocalDateTime createdAt;
    
    private String createdBy;
    
    public enum PenaltyType {
        MONEY, COMMUNITY_SERVICE, BAN_TEMPORARY, WARNING
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isPaid == null) {
            isPaid = false;
        }
        if (penaltyType == null) {
            penaltyType = PenaltyType.MONEY;
        }
    }
    
    // Constructors
    public Fine() {}
    
    public Fine(String userId, Long borrowId, Double amount, String reason) {
        this.userId = userId;
        this.borrowId = borrowId;
        this.amount = amount;
        this.reason = reason;
        this.penaltyType = PenaltyType.MONEY;
        this.isPaid = false;
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
    
    public PenaltyType getPenaltyType() { return penaltyType; }
    public void setPenaltyType(PenaltyType penaltyType) { this.penaltyType = penaltyType; }
    
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
        @Enumerated(EnumType.STRING)
    private DamageType damageType;  // Loại hư hỏng
    
    private Double bookPrice;        // Giá gốc sách
    
    private String damageDescription; // Mô tả hư hỏng
    
    public enum DamageType {
        LOST("Mất sách"),
        DAMAGED_HEAVY("Hư hỏng nặng - không phục hồi được"),
        DAMAGED_LIGHT("Hư hỏng nhẹ - có thể phục hồi");
        
        private final String description;
        
        DamageType(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    // Getters and Setters
    public DamageType getDamageType() { return damageType; }
    public void setDamageType(DamageType damageType) { this.damageType = damageType; }
    
    public Double getBookPrice() { return bookPrice; }
    public void setBookPrice(Double bookPrice) { this.bookPrice = bookPrice; }
    
    public String getDamageDescription() { return damageDescription; }
    public void setDamageDescription(String damageDescription) { this.damageDescription = damageDescription; }
}
