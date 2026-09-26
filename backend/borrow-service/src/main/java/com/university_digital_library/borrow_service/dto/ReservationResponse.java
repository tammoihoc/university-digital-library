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
            response.setId(reservation.getId());
            response.setUserId(reservation.getUserId());
            response.setBookId(reservation.getBookId());
            response.setReservationDate(reservation.getReservationDate());
            response.setPickupDate(reservation.getPickupDate());
            response.setExpiryDate(reservation.getExpiryDate());
            response.setStatus(reservation.getStatus() != null ? reservation.getStatus().name() : "PENDING");
            response.setNotes(reservation.getNotes());
            response.setConfirmedAt(reservation.getConfirmedAt());
            response.setConfirmedBy(reservation.getConfirmedBy());
        }
        return response;
    }
}
