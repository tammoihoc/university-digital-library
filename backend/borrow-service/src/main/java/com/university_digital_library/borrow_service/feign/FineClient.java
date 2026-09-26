package com.university_digital_library.borrow_service.feign;

import com.university_digital_library.borrow_service.config.FeignConfig;
import com.university_digital_library.borrow_service.dto.CreateFineRequest;
import com.university_digital_library.borrow_service.feign.fallback.FineClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "fine-service",
             fallback = FineClientFallback.class,
             configuration = FeignConfig.class)
public interface FineClient {

    @PostMapping("/fines")
    String createFine(
        @RequestBody CreateFineRequest request,
        @RequestHeader(value = "Authorization", required = false) String authorization
    );
}
