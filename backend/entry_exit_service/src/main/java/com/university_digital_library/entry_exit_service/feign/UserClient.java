// entry-exit-service/src/main/java/.../feign/UserClient.java
package com.university_digital_library.entry_exit_service.feign;

import com.university_digital_library.entry_exit_service.feign.fallback.UserClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.Map;

@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {
    
    @GetMapping("/users/profile/{username}")
    Map<String, Object> getUserProfile(
        @PathVariable("username") String username,
        @RequestHeader("Authorization") String authorization
    );
}
