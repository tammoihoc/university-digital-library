package com.university_digital_library.entry_exit_service.feign.fallback;

import com.university_digital_library.entry_exit_service.feign.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class UserClientFallback implements UserClient {

    @Override
    public Map<String, Object> getUserProfile(String username, String authorization) {
        log.warn("User Service unavailable for username: {}", username);
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("username", username);
        fallback.put("fullName", username);
        fallback.put("studentId", username);
        fallback.put("userType", "STUDENT");
        fallback.put("faculty", "Không xác định");
        return fallback;
    }

    @Override
    public Map<String, Object> getUserByStudentId(String studentId, String authorization) {
        log.warn("User Service unavailable for studentId: {}", studentId);
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("username", studentId);
        fallback.put("fullName", studentId);
        fallback.put("studentId", studentId);
        fallback.put("userType", "STUDENT");
        fallback.put("faculty", "Không xác định");
        return fallback;
    }
}
