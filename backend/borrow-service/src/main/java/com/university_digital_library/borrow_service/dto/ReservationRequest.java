// borrow-service/src/main/java/com/university_digital_library/borrow_service/dto/ReservationRequest.java
package com.university_digital_library.borrow_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservationRequest {
    private Long bookId;
    private LocalDateTime pickupDate;
    private String notes;
    
    // Getter và Setter
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    
    public LocalDateTime getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDateTime pickupDate) { this.pickupDate = pickupDate; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
