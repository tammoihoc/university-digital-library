// entry-exit-service/src/main/java/.../dto/EntryRecordDTO.java
package com.university_digital_library.entry_exit_service.dto;

import com.university_digital_library.entry_exit_service.model.EntryRecord;
import java.time.LocalDateTime;

public class EntryRecordDTO {
    private Long id;
    private String userId;
    private LocalDateTime entryTime;
    private String branch;
    
    public EntryRecordDTO() {}
    
    public static EntryRecordDTO fromEntity(EntryRecord record) {
        EntryRecordDTO dto = new EntryRecordDTO();
        dto.setId(record.getId());
        dto.setUserId(record.getUserId());
        dto.setEntryTime(record.getEntryTime());
        dto.setBranch(record.getBranch());
        return dto;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }
    
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
}
