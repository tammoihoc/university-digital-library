package com.university_digital_library.borrow_service.dto;

import com.university_digital_library.borrow_service.model.BorrowRecord;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BorrowResponse {
    private Long id;
    private String userId;
    private Long bookId;
    private LocalDateTime borrowedAt;
    private LocalDateTime dueDate;
    private LocalDateTime returnedAt;
    private String status;
    private Double fineAmount;
    private String notes;

    public static BorrowResponse fromEntity(BorrowRecord record) {
        BorrowResponse response = new BorrowResponse();
        response.setId(record.getId());
        response.setUserId(record.getUserId());
        response.setBookId(record.getBookId());
        response.setBorrowedAt(record.getBorrowedAt());
        response.setDueDate(record.getDueDate());
        response.setReturnedAt(record.getReturnedAt());
        response.setStatus(record.getStatus() != null ? record.getStatus().name() : "ACTIVE");
        response.setFineAmount(record.getFineAmount());
        response.setNotes(record.getNotes());
        return response;
    }
}
