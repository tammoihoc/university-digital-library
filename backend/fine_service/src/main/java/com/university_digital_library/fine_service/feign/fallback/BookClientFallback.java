// fine-service/src/main/java/.../feign/fallback/BookClientFallback.java
package com.university_digital_library.fine_service.feign.fallback;

import com.university_digital_library.fine_service.feign.BookClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class BookClientFallback implements BookClient {
    
    @Override
    public Map<String, Object> getBookPrice(Long bookId, String authorization) {
        log.warn("Book Service unavailable for bookId: {}, using default price", bookId);
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "Book Service unavailable");
        fallback.put("price", 150000.0);
        fallback.put("title", "Unknown Book");
        return fallback;
    }
}
