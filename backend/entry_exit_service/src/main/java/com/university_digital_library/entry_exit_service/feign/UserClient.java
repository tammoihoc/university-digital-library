package com.university_digital_library.entry_exit_service.feign;

import com.university_digital_library.entry_exit_service.config.FeignConfig;
import com.university_digital_library.entry_exit_service.feign.fallback.UserClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "user-service", fallback = UserClientFallback.class, configuration = FeignConfig.class)
public interface UserClient {

    @GetMapping("/users/profile/{username}")
    Map<String, Object> getUserProfile(
            @PathVariable("username") String username,
            @RequestHeader(value = "Authorization", required = false) String authorization
    );

    // ✅ THÊM: tìm user theo studentId
    @GetMapping("/users/student/{studentId}")
    Map<String, Object> getUserByStudentId(
            @PathVariable("studentId") String studentId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    );
}
