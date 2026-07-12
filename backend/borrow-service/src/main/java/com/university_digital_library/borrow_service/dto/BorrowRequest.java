// borrow-service/src/main/java/com/university_digital_library/borrow_service/dto/BorrowRequest.java
package com.university_digital_library.borrow_service.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class BorrowRequest {
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "Book ID is required")
    private Long bookId;
    
    private String notes;
    
    // Getter và Setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
