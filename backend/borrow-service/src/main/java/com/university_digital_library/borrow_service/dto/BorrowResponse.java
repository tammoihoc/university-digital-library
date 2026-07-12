// borrow-service/src/main/java/.../dto/BorrowResponse.java
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
        response.id = record.getId();
        response.userId = record.getUserId();
        response.bookId = record.getBookId();
        response.borrowedAt = record.getBorrowedAt();
        response.dueDate = record.getDueDate();
        response.returnedAt = record.getReturnedAt();
        response.status = record.getStatus() != null ? record.getStatus().name() : "ACTIVE";
        response.fineAmount = record.getFineAmount();
        response.notes = record.getNotes();
        return response;
    }
}
