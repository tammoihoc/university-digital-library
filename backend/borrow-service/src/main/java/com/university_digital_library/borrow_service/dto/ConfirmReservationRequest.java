
package com.university_digital_library.borrow_service.dto;

import lombok.Data;
import java.time.LocalDateTime;
@Data
public class ConfirmReservationRequest {
    private Long reservationId;
    private String librarianId;  // ID của thủ thư xác nhận
}
