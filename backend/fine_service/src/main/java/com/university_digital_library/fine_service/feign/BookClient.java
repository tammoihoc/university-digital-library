// fine-service/src/main/java/.../feign/BookClient.java
package com.university_digital_library.fine_service.feign;

import com.university_digital_library.fine_service.feign.fallback.BookClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.Map;

@FeignClient(name = "book-service", url = "http://localhost:8083", fallback = BookClientFallback.class)
public interface BookClient {
    
    @GetMapping("/books/{bookId}/price")
    Map<String, Object> getBookPrice(
        @PathVariable("bookId") Long bookId,
        @RequestHeader("Authorization") String authorization
    );
}
