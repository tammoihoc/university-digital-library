package com.university_digital_library.user_service.dto;

import com.university_digital_library.user_service.model.UserProfile;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateUserProfileRequest {
    // Thông tin cá nhân
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    
    private UserProfile.UserType userType;
    private UserProfile.Gender gender;
    private LocalDate dateOfBirth; // ĐẢM BẢO ĐÂY LÀ LocalDate
    
    // Thông tin sinh viên
    private String studentId;
    private String faculty;
    private String major;
    private Integer academicYear;
    
    // Thông tin giảng viên
    private String lecturerId;
    private String department;
    private String academicTitle;
    
    // Thông tin thủ thư
    private String librarianId;
    private String shift;
    private String position;
}
