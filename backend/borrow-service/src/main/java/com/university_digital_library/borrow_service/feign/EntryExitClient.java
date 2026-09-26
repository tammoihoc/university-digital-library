package com.university_digital_library.borrow_service.feign;

import com.university_digital_library.borrow_service.dto.EntryRecordDTO;
import com.university_digital_library.borrow_service.dto.EntryResponseDTO;
import com.university_digital_library.borrow_service.feign.fallback.EntryExitClientFallback;
import com.university_digital_library.borrow_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "entry-exit-service", 
             fallback = EntryExitClientFallback.class,
             configuration = FeignConfig.class) 
public interface EntryExitClient {
    
    @GetMapping("/entry-exit/current")
    List<EntryResponseDTO> getCurrentEntries(@RequestHeader(value = "Authorization", required = false) String authorization);
    
    @GetMapping("/entry-exit/history")
    List<EntryResponseDTO> getEntryHistory(
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate,
        @RequestHeader(value = "Authorization", required = false) String authorization
    );

    @GetMapping("/entry-exit/user/{userId}/status")
    Map<String, Object> getUserStatus(
        @PathVariable("userId") String userId,
        @RequestHeader(value = "Authorization", required = false) String authorization
    );
}
