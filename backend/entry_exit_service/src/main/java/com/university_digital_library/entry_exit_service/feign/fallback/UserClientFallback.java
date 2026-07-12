// entry-exit-service/src/main/java/.../feign/fallback/UserClientFallback.java
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
        log.warn("⚠️ User Service unavailable, returning fallback info for user: {}", username);
        
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("username", username);
        fallback.put("fullName", username);  // ✅ Dùng username làm fullName
        fallback.put("userType", "STUDENT");
        fallback.put("studentId", "N/A");    // ✅ Đổi từ UNKNOWN thành N/A
        fallback.put("faculty", "N/A");      // ✅ Đổi từ Unknown thành N/A
        fallback.put("major", "N/A");        // ✅ Đổi từ Unknown thành N/A
        fallback.put("email", "N/A");
        fallback.put("phone", "N/A");
        
        return fallback;
    }
}
