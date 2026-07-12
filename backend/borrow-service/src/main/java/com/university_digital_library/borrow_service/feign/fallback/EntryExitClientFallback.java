// borrow-service/src/main/java/.../feign/fallback/EntryExitClientFallback.java
package com.university_digital_library.borrow_service.feign.fallback;

import com.university_digital_library.borrow_service.dto.EntryRecordDTO;
import com.university_digital_library.borrow_service.dto.EntryResponseDTO;
import com.university_digital_library.borrow_service.feign.EntryExitClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class EntryExitClientFallback implements EntryExitClient {
    
    @Override
    public List<EntryResponseDTO> getCurrentEntries(String authorization) {
        log.warn("Entry-Exit Service unavailable for getCurrentEntries, returning empty list");
        return new ArrayList<>();
    }
    
    @Override
    public List<EntryRecordDTO> getEntryHistory(String startDate, String endDate, String authorization) {
        log.warn("Entry-Exit Service unavailable, returning empty list");
        return new ArrayList<>();
    }
}
