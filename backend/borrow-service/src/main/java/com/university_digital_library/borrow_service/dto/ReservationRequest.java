package com.university_digital_library.borrow_service.dto;

import lombok.Data;

import java.time.LocalDate;  // ✅ thay LocalDateTime → LocalDate

@Data
public class ReservationRequest {
    private Long bookId;
    private LocalDate pickupDate;   // ✅ thay LocalDateTime → LocalDate
    private String notes;
}
