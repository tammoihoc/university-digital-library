// borrow-service/src/main/java/.../feign/EntryExitClient.java
package com.university_digital_library.borrow_service.feign;

import com.university_digital_library.borrow_service.dto.EntryRecordDTO;
import com.university_digital_library.borrow_service.dto.EntryResponseDTO;
import com.university_digital_library.borrow_service.feign.fallback.EntryExitClientFallback;
import com.university_digital_library.borrow_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "entry-exit-service", 
             fallback = EntryExitClientFallback.class,
             configuration = FeignConfig.class) 
public interface EntryExitClient {
    
    @GetMapping("/entry-exit/current")
    List<EntryResponseDTO> getCurrentEntries(@RequestHeader(value = "Authorization", required = false) String authorization);
    
    @GetMapping("/entry-exit/history")
    List<EntryRecordDTO> getEntryHistory(
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate,
        @RequestHeader(value = "Authorization", required = false) String authorization
    );
}
