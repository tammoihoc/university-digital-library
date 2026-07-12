// borrow-service/src/main/java/com/university_digital_library/borrow_service/model/BorrowRecord.java
package com.university_digital_library.borrow_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private Long bookId;
    
    @Column(nullable = false)
    private LocalDateTime borrowedAt;
    
    private LocalDateTime dueDate;
    
    private LocalDateTime returnedAt;
    
    @Enumerated(EnumType.STRING)
    private BorrowStatus status;
    
    private Double fineAmount;
    
    private String notes;
    
    @PrePersist
    protected void onCreate() {
        borrowedAt = LocalDateTime.now();
        dueDate = LocalDateTime.now().plusDays(14);
        if (status == null) {
            status = BorrowStatus.ACTIVE;
        }
        if (fineAmount == null) {
            fineAmount = 0.0;
        }
    }
    
    public enum BorrowStatus {
        ACTIVE,      // Đang mượn
        OVERDUE,     // Quá hạn
        RETURNED,    // Đã trả
        CANCELLED    // Đã hủy
    }
    
    // Getter và Setter thủ công (đảm bảo Lombok hoạt động)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    
    public LocalDateTime getBorrowedAt() { return borrowedAt; }
    public void setBorrowedAt(LocalDateTime borrowedAt) { this.borrowedAt = borrowedAt; }
    
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
    
    public BorrowStatus getStatus() { return status; }
    public void setStatus(BorrowStatus status) { this.status = status; }
    
    public Double getFineAmount() { return fineAmount; }
    public void setFineAmount(Double fineAmount) { this.fineAmount = fineAmount; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
