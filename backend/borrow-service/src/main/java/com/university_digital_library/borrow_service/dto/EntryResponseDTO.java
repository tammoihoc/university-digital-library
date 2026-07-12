// borrow-service/src/main/java/.../dto/EntryResponseDTO.java
package com.university_digital_library.borrow_service.dto;

import java.time.LocalDateTime;

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
    
    // Constructors
    public EntryResponseDTO() {}
    
    public EntryResponseDTO(Long id, String userId, String fullName, String studentId, 
                           String faculty, String major, String userType, 
                           LocalDateTime entryTime, String branch) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.studentId = studentId;
        this.faculty = faculty;
        this.major = major;
        this.userType = userType;
        this.entryTime = entryTime;
        this.branch = branch;
    }
    
    // Getters
    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getStudentId() { return studentId; }
    public String getFaculty() { return faculty; }
    public String getMajor() { return major; }
    public String getUserType() { return userType; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public String getBranch() { return branch; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setFaculty(String faculty) { this.faculty = faculty; }
    public void setMajor(String major) { this.major = major; }
    public void setUserType(String userType) { this.userType = userType; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }
    public void setBranch(String branch) { this.branch = branch; }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long id;
        private String userId;
        private String fullName;
        private String studentId;
        private String faculty;
        private String major;
        private String userType;
        private LocalDateTime entryTime;
        private String branch;
        
        public Builder id(Long id) { this.id = id; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder studentId(String studentId) { this.studentId = studentId; return this; }
        public Builder faculty(String faculty) { this.faculty = faculty; return this; }
        public Builder major(String major) { this.major = major; return this; }
        public Builder userType(String userType) { this.userType = userType; return this; }
        public Builder entryTime(LocalDateTime entryTime) { this.entryTime = entryTime; return this; }
        public Builder branch(String branch) { this.branch = branch; return this; }
        
        public EntryResponseDTO build() {
            return new EntryResponseDTO(id, userId, fullName, studentId, faculty, major, userType, entryTime, branch);
        }
    }
}
