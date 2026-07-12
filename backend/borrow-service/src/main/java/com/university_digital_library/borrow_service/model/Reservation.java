package com.university_digital_library.borrow_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// Thêm @Builder.Default cho các field có giá trị mặc định
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
    @Builder.Default
    private LocalDateTime reservationDate = LocalDateTime.now();
    
    @Column(nullable = false)
    private LocalDateTime pickupDate;
    
    @Column(nullable = false)
    private LocalDateTime expiryDate;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.CONFIRMED;
    
    private String notes;
    
    private LocalDateTime confirmedAt;
    private String confirmedBy;
    
    private LocalDateTime cancelledAt;
    private String cancelReason;
    
    public enum ReservationStatus {
        PENDING, CONFIRMED, CANCELLED, EXPIRED, COMPLETED
    }
}
