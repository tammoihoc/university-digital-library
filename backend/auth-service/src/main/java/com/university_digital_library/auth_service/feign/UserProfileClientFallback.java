package com.university_digital_library.auth_service.feign;

import com.university_digital_library.auth_service.dto.CreateUserProfileRequest;
import org.springframework.stereotype.Component;

@Component
public class UserProfileClientFallback implements UserProfileClient {
    
    @Override
    public String createUserProfile(CreateUserProfileRequest request) {
        throw new RuntimeException("User service is temporarily unavailable");
    }
}
