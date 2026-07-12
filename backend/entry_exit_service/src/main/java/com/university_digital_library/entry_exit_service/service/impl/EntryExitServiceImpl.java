// entry-exit-service/src/main/java/.../service/impl/EntryExitServiceImpl.java
package com.university_digital_library.entry_exit_service.service.impl;

import com.university_digital_library.entry_exit_service.dto.EntryResponseDTO;
import com.university_digital_library.entry_exit_service.dto.EntryRecordDTO;
import com.university_digital_library.entry_exit_service.feign.UserClient;
import com.university_digital_library.entry_exit_service.model.EntryRecord;
import com.university_digital_library.entry_exit_service.repository.EntryExitRepository;
import com.university_digital_library.entry_exit_service.service.EntryExitService;
import com.university_digital_library.entry_exit_service.service.LibraryHoursService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntryExitServiceImpl implements EntryExitService {
    
    private final EntryExitRepository repository;
    private final UserClient userClient;
    private final LibraryHoursService libraryHoursService;
    
// entry-exit-service/src/main/java/.../service/impl/EntryExitServiceImpl.java
@Override
@Transactional
public EntryResponseDTO recordEntry(String userId, String branch, String authorization) {
    log.info("Recording entry for user: {} at branch: {}", userId, branch);
    
    // KIỂM TRA GIỜ MỞ CỬA
    LocalDateTime now = LocalDateTime.now();
    if (!libraryHoursService.isLibraryOpen(branch, now)) {
        String hoursInfo = libraryHoursService.getOpeningHours(branch);
        throw new RuntimeException("Thư viện " + branch + " đang đóng cửa!\n" + hoursInfo);
    }
    
    // ✅ CHỈ KIỂM TRA TRONG NGÀY HÔM NAY, KHÔNG KIỂM TRA NGÀY CŨ
    LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);
    
    // Kiểm tra user đã ở trong thư viện này HÔM NAY chưa
    List<EntryRecord> activeToday = repository.findByUserIdAndBranchAndEntryTimeBetween(
        userId, branch, startOfDay, endOfDay);
    
    if (!activeToday.isEmpty()) {
        throw new RuntimeException("Bạn đã vào thư viện " + branch + " hôm nay rồi! Mỗi ngày chỉ được vào 1 lần/cơ sở.");
    }
    
    // Lấy thông tin user
    Map<String, Object> userInfo = getUserInfo(userId, authorization);
    log.info("User info from User Service: {}", userInfo);
    
    // Tạo record mới
    EntryRecord record = new EntryRecord(userId, branch);
    EntryRecord saved = repository.save(record);
    log.info("Entry recorded with id: {} at branch: {}", saved.getId(), saved.getBranch());
    
    // Tạo response
    return EntryResponseDTO.builder()
            .id(saved.getId())
            .userId(userId)
            .fullName((String) userInfo.getOrDefault("fullName", userId))
            .studentId((String) userInfo.getOrDefault("studentId", "N/A"))
            .faculty((String) userInfo.getOrDefault("faculty", "N/A"))
            .major((String) userInfo.getOrDefault("major", "N/A"))
            .userType((String) userInfo.getOrDefault("userType", "STUDENT"))
            .entryTime(saved.getEntryTime())
            .branch(branch)
            .build();
}
// entry-exit-service/src/main/java/.../service/impl/EntryExitServiceImpl.java
// entry-exit-service/src/main/java/.../service/impl/EntryExitServiceImpl.java
private Map<String, Object> getUserInfo(String userId, String authorization) {
    Map<String, Object> defaultInfo = new HashMap<>();
    defaultInfo.put("fullName", userId);
    defaultInfo.put("studentId", "N/A");
    defaultInfo.put("faculty", "N/A");
    defaultInfo.put("major", "N/A");
    defaultInfo.put("userType", "STUDENT");
    
    // ✅ KIỂM TRA AUTHORIZATION
    if (authorization == null || authorization.isEmpty()) {
        log.warn("Authorization is NULL or EMPTY for user: {}", userId);
        return defaultInfo;
    }
    
    if (!authorization.startsWith("Bearer ")) {
        log.warn("Authorization does not start with 'Bearer ' for user: {}", userId);
        return defaultInfo;
    }
    
    try {
        log.info("Calling User Service for user: {} with token: {}", userId, authorization.substring(0, Math.min(50, authorization.length())) + "...");
        Map<String, Object> userInfo = userClient.getUserProfile(userId, authorization);
        
        if (userInfo != null && !userInfo.containsKey("error")) {
            log.info("Successfully got user info for: {}", userId);
            return userInfo;
        } else {
            log.warn("User info contains error for: {}", userId);
        }
    } catch (Exception e) {
        log.error("Exception when calling User Service for {}: {}", userId, e.getMessage());
    }
    return defaultInfo;
}
    
// entry-exit-service/src/main/java/.../service/impl/EntryExitServiceImpl.java
@Override
public List<EntryResponseDTO> getCurrentEntriesWithUserInfo(String authorization) {
    log.info("=== GET CURRENT ENTRIES CALLED ===");
    log.info("Authorization in service: {}", authorization != null ? "present (first 50 chars: " + authorization.substring(0, Math.min(50, authorization.length())) + ")" : "NULL");
    
    List<EntryRecord> records = repository.findAll();
    log.info("Found {} records in database", records.size());
    
    List<EntryResponseDTO> result = new ArrayList<>();
    
    for (EntryRecord record : records) {
        log.info("Processing user: {}", record.getUserId());
        
        // ✅ THỬ GỌI USER SERVICE
        Map<String, Object> userInfo = getUserInfo(record.getUserId(), authorization);
        
        log.info("User info for {}: fullName={}, studentId={}, faculty={}, major={}", 
                 record.getUserId(),
                 userInfo.get("fullName"),
                 userInfo.get("studentId"),
                 userInfo.get("faculty"),
                 userInfo.get("major"));
        
        result.add(EntryResponseDTO.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .fullName((String) userInfo.getOrDefault("fullName", record.getUserId()))
                .studentId((String) userInfo.getOrDefault("studentId", "N/A"))
                .faculty((String) userInfo.getOrDefault("faculty", "N/A"))
                .major((String) userInfo.getOrDefault("major", "N/A"))
                .userType((String) userInfo.getOrDefault("userType", "STUDENT"))
                .entryTime(record.getEntryTime())
                .branch(record.getBranch())
                .build());
    }
    return result;
}
// entry-exit-service/src/main/java/.../service/impl/EntryExitServiceImpl.java
// Thêm các method còn thiếu

@Override
public List<EntryRecordDTO> getEntryHistory(String startDate, String endDate) {
    log.info("Getting entry history from {} to {}", startDate, endDate);
    
    LocalDateTime start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE).atStartOfDay();
    LocalDateTime end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE).atTime(23, 59, 59);
    
    return repository.findByEntryTimeBetween(start, end).stream()
            .map(EntryRecordDTO::fromEntity)
            .collect(Collectors.toList());
}
public List<EntryRecordDTO> getUserHistory(String userId) {
    log.info("Getting entry history for user: {}", userId);
    return repository.findByUserIdOrderByEntryTimeDesc(userId).stream()
            .map(EntryRecordDTO::fromEntity)
            .collect(Collectors.toList());
}
    
    @Override
    public Map<String, Object> getDailyStats() {
        log.info("Getting daily statistics");
        
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayEntries", repository.countByEntryTimeBetween(startOfDay, endOfDay));
        
        return stats;
    }
    
    @Override
    public long getCurrentCount() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        return repository.countByEntryTimeBetween(startOfDay, endOfDay);
    }
}
