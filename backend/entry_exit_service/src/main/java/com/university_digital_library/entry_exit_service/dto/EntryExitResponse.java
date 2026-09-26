// entry-exit-service/src/main/java/.../dto/EntryResponseDTO.java
package com.university_digital_library.entry_exit_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntryExitResponse {
    private Long id;
    private String userId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private String branch;
    private String status;
    private String fullName;
    private String studentId;
    private String userType;
    private String faculty;
    private String major;
    private String message;
}
