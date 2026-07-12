package com.university_digital_library.auth_service.feign;

import com.university_digital_library.auth_service.dto.CreateUserProfileRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", url = "localhost:8082")
public interface UserProfileClient {
    
    @PostMapping("/users/profile")  // ĐÚNG PATH
    String createUserProfile(@RequestBody CreateUserProfileRequest request);
}
