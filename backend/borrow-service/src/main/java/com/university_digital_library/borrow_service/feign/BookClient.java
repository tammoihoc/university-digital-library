package com.university_digital_library.borrow_service.feign;

import com.university_digital_library.borrow_service.feign.fallback.BookClientFallback;
import com.university_digital_library.borrow_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "book-service",
             fallback = BookClientFallback.class,
             configuration = FeignConfig.class)
public interface BookClient {

    @GetMapping("/books/{bookId}/availability")
    Map<String, Object> getBookAvailability(
        @PathVariable("bookId") Long bookId,
        @RequestHeader(value = "Authorization", required = false) String authorization
    );

    // ✅ Mới: lấy thông tin chi tiết sách (bao gồm libraryBranchId)
    @GetMapping("/books/{bookId}")
    Map<String, Object> getBookDetails(
        @PathVariable("bookId") Long bookId,
        @RequestHeader(value = "Authorization", required = false) String authorization
    );

    @GetMapping("/books/{bookId}/location")
    Map<String, Object> getBookLocation(
        @PathVariable("bookId") Long bookId,
        @RequestHeader(value = "Authorization", required = false) String authorization
    );

    @PostMapping("/books/{bookId}/update-available")
    String updateAvailableCopies(
        @PathVariable("bookId") Long bookId,
        @RequestParam("newAvailable") Integer newAvailable,
        @RequestHeader(value = "Authorization", required = false) String authorization
    );

    // Tăng/giảm nguyên tử ở book-service — tránh race condition so với cách cũ
    // (đọc availableCopies rồi tự trừ/cộng ở borrow-service rồi ghi lại).
    @PostMapping("/books/{bookId}/decrement-available")
    Map<String, Object> decrementAvailableCopies(
        @PathVariable("bookId") Long bookId,
        @RequestHeader(value = "Authorization", required = false) String authorization
    );

    @PostMapping("/books/{bookId}/increment-available")
    Map<String, Object> incrementAvailableCopies(
        @PathVariable("bookId") Long bookId,
        @RequestHeader(value = "Authorization", required = false) String authorization
    );
}
