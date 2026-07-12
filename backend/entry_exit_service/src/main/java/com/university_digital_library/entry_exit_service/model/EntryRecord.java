// entry-exit-service/src/main/java/.../model/EntryRecord.java
package com.university_digital_library.entry_exit_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "entry_exit_records")
public class EntryRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private LocalDateTime entryTime;
    
    @Column(nullable = false)
    private String branch;
    
    @PrePersist
    protected void onCreate() {
        entryTime = LocalDateTime.now();
    }
    
    public EntryRecord() {}
    
    public EntryRecord(String userId, String branch) {
        this.userId = userId;
        this.branch = branch;
        this.entryTime = LocalDateTime.now();
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
