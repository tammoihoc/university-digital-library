package com.university_digital_library.borrow_service.feign.fallback;

import com.university_digital_library.borrow_service.dto.EntryRecordDTO;
import com.university_digital_library.borrow_service.dto.EntryResponseDTO;
import com.university_digital_library.borrow_service.feign.EntryExitClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class EntryExitClientFallback implements EntryExitClient {
    
    @Override
    public List<EntryResponseDTO> getCurrentEntries(String authorization) {
        log.warn("Entry-Exit Service unavailable for getCurrentEntries, returning empty list");
        return new ArrayList<>();
    }
    
    @Override
    public List<EntryResponseDTO> getEntryHistory(String startDate, String endDate, String authorization) {
        log.warn("Entry-Exit Service unavailable, returning empty list");
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getUserStatus(String userId, String authorization) {
        log.warn("Entry-Exit Service unavailable for user status: {}", userId);
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("checkedIn", false);
        fallback.put("status", "OUTSIDE");
        fallback.put("branch", "B");
        return fallback;
    }
}
