// auth-service/src/main/java/.../dto/CreateUserProfileRequest.java
package com.university_digital_library.auth_service.dto;

import lombok.Data;

@Data
public class CreateUserProfileRequest {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String userType; // STUDENT, LECTURER, LIBRARIAN, ADMIN
    
    private String faculty;
    private String major;
    private Integer academicYear;
    private String department;
    private String academicTitle;
    private String shift;
    private String position;
}
