package com.university_digital_library.user_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateBorrowLimitRequest {
    private Integer maxBorrowLimit;
    private Boolean isLocked;
    private LocalDateTime lockedUntil;
    private String reason;
}
