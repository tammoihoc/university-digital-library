package com.university_digital_library.user_service.dto;

import com.university_digital_library.user_service.model.UserProfile;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserProfileRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
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
