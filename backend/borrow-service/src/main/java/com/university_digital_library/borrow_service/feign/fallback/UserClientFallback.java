// borrow-service/src/main/java/.../feign/fallback/UserClientFallback.java
package com.university_digital_library.borrow_service.feign.fallback;

import com.university_digital_library.borrow_service.feign.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class UserClientFallback implements UserClient {
    
    @Override
    public Map<String, Object> getUserBorrowInfo(String userId, String authorization) {
        log.warn("User Service unavailable for userId: {}", userId);
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("error", "User Service is unavailable");
        fallbackResponse.put("canBorrowMore", false);
        fallbackResponse.put("currentBorrowed", 0);
        fallbackResponse.put("maxBorrowLimit", 2);
        return fallbackResponse;
    }
    
    @Override
    public String updateBorrowCount(String userId, Integer newCount, String authorization) {
        log.warn("User Service unavailable, cannot update borrow count for userId: {}", userId);
        throw new RuntimeException("User Service is unavailable");
    }
}
