package com.university_digital_library.entry_exit_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EntryExitRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    private String branch; // 'B' hoặc 'E'
    
    private String fullName;
    private String userType;
    private String studentId;
    private String faculty;
    private String major;
}
