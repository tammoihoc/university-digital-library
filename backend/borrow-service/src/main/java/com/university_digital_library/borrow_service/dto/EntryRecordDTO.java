// borrow-service/src/main/java/.../dto/EntryRecordDTO.java
package com.university_digital_library.borrow_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EntryRecordDTO {
    private Long id;
    private String userId;
    private LocalDateTime entryTime;
    private String branch;
}
