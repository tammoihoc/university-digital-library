package com.university_digital_library.user_service.dto;

import com.university_digital_library.user_service.model.UserProfile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateUserProfileRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String phone;
    private String address;
    
    @NotNull(message = "User type is required")
    private UserProfile.UserType userType;
    
    private UserProfile.Gender gender;
    private LocalDate dateOfBirth;
    
    // Student fields
    private String studentId;
    private String faculty;
    private String major;
    private Integer academicYear;
    
    // Lecturer fields
    private String lecturerId;
    private String department;
    private String academicTitle;
    
    // Librarian fields
    private String librarianId;
    private String shift;
    private String position;
}
