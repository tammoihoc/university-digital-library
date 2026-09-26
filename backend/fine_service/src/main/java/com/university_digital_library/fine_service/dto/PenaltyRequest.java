// fine-service/src/main/java/com/university_digital_library/fine_service/dto/PenaltyRequest.java
package com.university_digital_library.fine_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PenaltyRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Penalty type is required")
    private String penaltyType;  // LATE_RETURN, DAMAGED, LOST, MULTIPLE_VIOLATIONS
    
    private String reason;
    
    private String level;  // WARNING, MINOR, MODERATE, SEVERE, PERMANENT
    
    private Integer newBorrowLimit;
    
    private LocalDateTime lockedUntil;
}
