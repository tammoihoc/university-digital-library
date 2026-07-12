// borrow-service/src/main/java/.../feign/UserClient.java
package com.university_digital_library.borrow_service.feign;

import com.university_digital_library.borrow_service.feign.fallback.UserClientFallback;
import com.university_digital_library.borrow_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

@FeignClient(name = "user-service", 
             fallback = UserClientFallback.class,
             configuration = FeignConfig.class)
public interface UserClient {
    
    @GetMapping("/users/{userId}/borrow-info")
    Map<String, Object> getUserBorrowInfo(
        @PathVariable("userId") String userId,
        @RequestHeader(value = "Authorization", required = false) String authorization
    );
    
    @PostMapping("/users/{userId}/borrow-count")
    String updateBorrowCount(
        @PathVariable("userId") String userId,
        @RequestParam("newCount") Integer newCount,
        @RequestHeader(value = "Authorization", required = false) String authorization
    );
}
