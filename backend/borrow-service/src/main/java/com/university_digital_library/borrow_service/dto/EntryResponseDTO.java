package com.university_digital_library.borrow_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntryResponseDTO {
    private Long id;
    private String userId;
    private String fullName;
    private String studentId;
    private String faculty;
    private String major;
    private String userType;
    private LocalDateTime entryTime;
    private String branch;
    private String status;  // INSIDE, OUTSIDE
}
