package com.university_digital_library.common_library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private String username;
    private String fullName;
    private String email;
    private Set<String> roles;
    private String userType;
}
