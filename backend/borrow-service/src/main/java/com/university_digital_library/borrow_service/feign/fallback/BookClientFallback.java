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
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "Book Service unavailable");
        fallback.put("available", false);
        fallback.put("availablePhysicalCopies", 0);
        fallback.put("canBeBorrowed", false);
        return fallback;
    }

    @Override
    public Map<String, Object> getBookDetails(Long bookId, String authorization) {
        log.warn("Book Service unavailable for getBookDetails, bookId: {}", bookId);
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("id", bookId);
        fallback.put("title", "Unknown");
        fallback.put("libraryBranchId", 1);
        fallback.put("availablePhysicalCopies", 0);
        return fallback;
    }

    @Override
    public Map<String, Object> getBookLocation(Long bookId, String authorization) {
        log.warn("Book Service unavailable for getBookLocation, bookId: {}", bookId);
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "Book Service unavailable");
        fallback.put("fullCode", "Không xác định");
        return fallback;
    }

    @Override
    public String updateAvailableCopies(Long bookId, Integer newAvailable, String authorization) {
        log.warn("Book Service unavailable, cannot update available copies for bookId: {}", bookId);
        throw new RuntimeException("Book Service is unavailable");
    }

    @Override
    public Map<String, Object> decrementAvailableCopies(Long bookId, String authorization) {
        log.warn("Book Service unavailable, cannot decrement available copies for bookId: {}", bookId);
        throw new RuntimeException("Book Service is unavailable");
    }

    @Override
    public Map<String, Object> incrementAvailableCopies(Long bookId, String authorization) {
        log.warn("Book Service unavailable, cannot increment available copies for bookId: {}", bookId);
        throw new RuntimeException("Book Service is unavailable");
    }
}
