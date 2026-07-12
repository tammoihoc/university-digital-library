// borrow-service/src/main/java/.../feign/fallback/BookClientFallback.java
package com.university_digital_library.borrow_service.feign.fallback;

import com.university_digital_library.borrow_service.feign.BookClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class BookClientFallback implements BookClient {
    
    @Override
    public Map<String, Object> getBookAvailability(Long bookId, String authorization) {
        log.warn("Book Service unavailable for bookId: {}", bookId);
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("error", "Book Service is unavailable");
        fallbackResponse.put("available", false);
        fallbackResponse.put("availablePhysicalCopies", 0);
        fallbackResponse.put("canBeBorrowed", false);
        return fallbackResponse;
    }
    
    @Override
    public Map<String, Object> getBookLocation(Long bookId, String authorization) {
        log.warn("Book Service unavailable for getBookLocation, bookId: {}", bookId);
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("error", "Book Service is unavailable");
        fallbackResponse.put("fullCode", "Không xác định");
        fallbackResponse.put("branch", "B");
        return fallbackResponse;
    }
    
    @Override
    public String updateAvailableCopies(Long bookId, Integer newAvailable, String authorization) {
        log.warn("Book Service unavailable, cannot update available copies for bookId: {}", bookId);
        throw new RuntimeException("Book Service is unavailable");
    }
}
