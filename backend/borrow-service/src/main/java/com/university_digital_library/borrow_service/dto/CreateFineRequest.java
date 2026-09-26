package com.university_digital_library.borrow_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO nội bộ borrow-service dùng để gọi sang fine_service khi trả sách trễ hạn.
 * Cấu trúc field khớp với CreateFineRequest bên fine_service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFineRequest {
    private String userId;
    private Long borrowId;
    private Double amount;
    private String penaltyType;
    private String reason;
}
