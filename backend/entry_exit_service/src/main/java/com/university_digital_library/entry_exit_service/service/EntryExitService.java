// entry-exit-service/src/main/java/.../service/EntryExitService.java
package com.university_digital_library.entry_exit_service.service;

import com.university_digital_library.entry_exit_service.dto.EntryResponseDTO;
import com.university_digital_library.entry_exit_service.dto.EntryRecordDTO;
import java.util.List;
import java.util.Map;

public interface EntryExitService {
    
    EntryResponseDTO recordEntry(String userId, String branch, String authorization);
    
    List<EntryResponseDTO> getCurrentEntriesWithUserInfo(String authorization);
    
    List<EntryRecordDTO> getEntryHistory(String startDate, String endDate);
    
    List<EntryRecordDTO> getUserHistory(String userId);
    
    Map<String, Object> getDailyStats();
    
    long getCurrentCount();
}
