// borrow-service/src/main/java/.../dto/DirectBorrowRequest.java
package com.university_digital_library.borrow_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DirectBorrowRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "Book ID is required")
    private Long bookId;
    
    private String notes;
    
    // Getters
    public String getUserId() { return userId; }
    public Long getBookId() { return bookId; }
    public String getNotes() { return notes; }
    
    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public void setNotes(String notes) { this.notes = notes; }
}
