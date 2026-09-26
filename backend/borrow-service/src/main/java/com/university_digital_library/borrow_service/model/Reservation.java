package com.university_digital_library.borrow_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private Long bookId;
    
    @Column(nullable = false)
    private LocalDateTime reservationDate;
    
    @Column(nullable = false)
    private LocalDateTime pickupDate;
    
    @Column(nullable = false)
    private LocalDateTime expiryDate;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.CONFIRMED;
    
    private String notes;
    
    private String bookTitle;
    private String bookAuthor;
    private String bookLocation;
    
    private LocalDateTime confirmedAt;
    private String confirmedBy;
    
    private LocalDateTime cancelledAt;
    private String cancelReason;
    
    @PrePersist
    protected void onCreate() {
        if (reservationDate == null) {
            reservationDate = LocalDateTime.now();
        }
        if (status == null) {
            status = ReservationStatus.CONFIRMED;
        }
        if (expiryDate == null) {
            expiryDate = LocalDateTime.now().plusDays(1);
        }
    }
    
    public enum ReservationStatus {
        PENDING, CONFIRMED, CANCELLED, EXPIRED, COMPLETED
    }
}
