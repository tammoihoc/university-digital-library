// fine-service/src/main/java/.../dto/ReportDamageRequest.java
package com.university_digital_library.fine_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportDamageRequest {
    
    @NotNull(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "Borrow ID is required")
    private Long borrowId;
    
    @NotNull(message = "Book ID is required")
    private Long bookId;
    
    @NotNull(message = "Damage type is required")
    private String damageType;  // LOST, DAMAGED_HEAVY, DAMAGED_LIGHT
    
    private String description;  // Mô tả chi tiết hư hỏng
}
