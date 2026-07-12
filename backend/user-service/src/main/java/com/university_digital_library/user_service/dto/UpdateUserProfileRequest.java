package com.university_digital_library.user_service.dto;

import com.university_digital_library.user_service.model.UserProfile;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateUserProfileRequest {
    // Thông tin cá nhân - CÓ THỂ CẬP NHẬT
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    
    private UserProfile.Gender gender;
    private LocalDate dateOfBirth;
    
    // Thông tin sinh viên - CÓ THỂ CẬP NHẬT
    private String studentId;
    private String faculty;
    private String major;
    private Integer academicYear;
    
    // Thông tin giảng viên - CÓ THỂ CẬP NHẬT
    private String lecturerId;
    private String department;
    private String academicTitle;
    
    // Thông tin thủ thư - CÓ THỂ CẬP NHẬT
    private String librarianId;
    private String shift;
    private String position;
}
