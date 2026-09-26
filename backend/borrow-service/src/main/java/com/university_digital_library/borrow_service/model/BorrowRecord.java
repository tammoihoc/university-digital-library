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
    @Builder.Default
    private BorrowStatus status = BorrowStatus.ACTIVE;
    
    private Double fineAmount;
    
    private String notes;
    
    private String borrowedLocation;
    
    @Column(unique = true)
    private String reservationId;
    
    @PrePersist
    protected void onCreate() {
        if (borrowedAt == null) {
            borrowedAt = LocalDateTime.now();
        }
        if (dueDate == null) {
            dueDate = LocalDateTime.now().plusDays(14);
        }
        if (status == null) {
            status = BorrowStatus.ACTIVE;
        }
        if (fineAmount == null) {
            fineAmount = 0.0;
        }
    }
    
    public enum BorrowStatus {
        ACTIVE, OVERDUE, RETURNED, CANCELLED
    }
}
