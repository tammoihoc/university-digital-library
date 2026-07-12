// borrow-service/src/main/java/.../dto/ReservationResponse.java
package com.university_digital_library.borrow_service.dto;

import com.university_digital_library.borrow_service.model.Reservation;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservationResponse {
    private Long id;
    private String userId;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private LocalDateTime reservationDate;
    private LocalDateTime pickupDate;
    private LocalDateTime expiryDate;
    private String status;
    private String notes;
    private Integer queuePosition;
    private LocalDateTime confirmedAt;
    private String confirmedBy;
    
    public static ReservationResponse fromEntity(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        if (reservation != null) {
            response.id = reservation.getId();
            response.userId = reservation.getUserId();
            response.bookId = reservation.getBookId();
            response.reservationDate = reservation.getReservationDate();
            response.pickupDate = reservation.getPickupDate();
            response.expiryDate = reservation.getExpiryDate();
            response.status = reservation.getStatus() != null ? reservation.getStatus().name() : "PENDING";
            response.notes = reservation.getNotes();
            response.confirmedAt = reservation.getConfirmedAt();
            response.confirmedBy = reservation.getConfirmedBy();
        }
        return response;
    }
}
