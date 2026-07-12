// auth-service/src/main/java/.../dto/RegisterRequest.java
package com.university_digital_library.auth_service.dto;

import lombok.Data;
import java.util.Set;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private Set<String> roles;
    private CreateUserProfileRequest userProfile;
    
    public void validate() {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("At least one role is required");
        }
        if (userProfile == null) {
            throw new IllegalArgumentException("User profile information is required");
        }
        
        // Map roles to userType
        String userType = mapRolesToUserType(roles);
        userProfile.setUserType(userType);
        
        // ✅ KHÔNG CẦN GÁN studentId/lecturerId/librarianId nữa
        // User Service sẽ tự tạo
    }
    
    private String mapRolesToUserType(Set<String> roles) {
        if (roles.contains("ADMIN")) return "ADMIN";
        if (roles.contains("LIBRARIAN")) return "LIBRARIAN"; 
        if (roles.contains("LECTURER")) return "LECTURER";
        if (roles.contains("STUDENT")) return "STUDENT";
        throw new IllegalArgumentException("Invalid roles combination");
    }
}
