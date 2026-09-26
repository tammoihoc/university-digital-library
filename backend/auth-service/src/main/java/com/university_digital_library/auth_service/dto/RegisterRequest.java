package com.university_digital_library.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;
    
    // Yêu cầu tối thiểu 8 ký tự, có chữ hoa, chữ thường và số — 6 ký tự đơn giản
    // trước đây quá yếu để chống dò mật khẩu (brute-force/dictionary attack).
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    private String password;

    // ĐÃ ĐỔI THIẾT KẾ: /auth/register giờ CHỈ ADMIN gọi được (xem SecurityConfig +
    // @PreAuthorize trên AuthController), đúng quy trình thực tế của trường —
    // sinh viên nộp hồ sơ nhập học cung cấp thông tin, phòng đào tạo/thư viện
    // (admin) mới là người tạo tài khoản, không có chuyện ai cũng tự đăng ký được.
    // Vì chỉ ADMIN mới gọi được endpoint này, cho phép ADMIN chỉ định vai trò của
    // tài khoản mới tạo (STUDENT/LIBRARIAN/LECTURER/ADMIN) — khác hẳn thời còn tự
    // đăng ký công khai, khi đó role bị bỏ hẳn để tránh leo thang đặc quyền.
    @Pattern(
        regexp = "^(STUDENT|LIBRARIAN|LECTURER|ADMIN)$",
        message = "role must be one of STUDENT, LIBRARIAN, LECTURER, ADMIN"
    )
    private String role;   // Không bắt buộc — bỏ trống sẽ mặc định STUDENT

    // Các field dưới đây tuỳ chọn, dùng để tạo profile bên user-service ngay khi đăng ký
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String faculty;
    private String major;
    private String department;
}
