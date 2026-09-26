package com.university_digital_library.borrow_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Book ID is required")
    private Long bookId;

    private String notes;
}
