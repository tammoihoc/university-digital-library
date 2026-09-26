// fine-service/src/main/java/com/university_digital_library/fine_service/dto/FineRequest.java
package com.university_digital_library.fine_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class FineRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    private Long borrowId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;
    
    private String penaltyType;
    private String reason;
}
