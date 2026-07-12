// entry-exit-service/src/main/java/.../controller/EntryExitController.java
package com.university_digital_library.entry_exit_service.controller;

import com.university_digital_library.entry_exit_service.dto.EntryResponseDTO;
import com.university_digital_library.entry_exit_service.dto.EntryRecordDTO;
import com.university_digital_library.entry_exit_service.service.EntryExitService;
import com.university_digital_library.entry_exit_service.service.LibraryHoursService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/entry-exit")
@RequiredArgsConstructor
@Slf4j
public class EntryExitController {
    
    private final EntryExitService entryExitService;
    private final LibraryHoursService libraryHoursService;
    
@PostMapping("/entry")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<EntryResponseDTO> recordEntry(
        @RequestHeader(value = "X-Branch", required = false, defaultValue = "B") String branch,
        @RequestHeader("Authorization") String authorization,
        Authentication auth) {
    
    if (!branch.equals("B") && !branch.equals("E")) {
        throw new RuntimeException("Branch không hợp lệ! Chỉ chấp nhận B (Thư viện chính) hoặc E (Thư viện Quận 9)");
    }
    
    String userId = auth.getName();
    log.info("User {} entering library at branch {}", userId, branch);
    
    // ✅ Truyền token xuống service
    EntryResponseDTO response = entryExitService.recordEntry(userId, branch, authorization);
    return ResponseEntity.ok(response);
}
    
// entry-exit-service/src/main/java/.../controller/EntryExitController.java
// entry-exit-service/src/main/java/.../controller/EntryExitController.java
@GetMapping("/current")
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
public ResponseEntity<List<EntryResponseDTO>> getCurrentEntries(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        HttpServletRequest request) {
    
    // ✅ LẤY TOKEN TỪ REQUEST NẾU AUTHORIZATION NULL
    if (authorization == null) {
        authorization = request.getHeader("Authorization");
    }
    
    log.info("Authorization header in controller: {}", authorization != null ? "present (length=" + authorization.length() + ")" : "NULL");
    
    List<EntryResponseDTO> result = entryExitService.getCurrentEntriesWithUserInfo(authorization);
    return ResponseEntity.ok(result);
}
    
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<EntryRecordDTO>> getEntryHistory(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        if (startDate == null || endDate == null) {
            java.time.LocalDate end = java.time.LocalDate.now();
            java.time.LocalDate start = end.minusDays(7);
            startDate = start.toString();
            endDate = end.toString();
        }
        
        return ResponseEntity.ok(entryExitService.getEntryHistory(startDate, endDate));
    }
    
    @GetMapping("/my-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EntryRecordDTO>> getMyHistory(Authentication auth) {
        String userId = auth.getName();
        return ResponseEntity.ok(entryExitService.getUserHistory(userId));
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<EntryRecordDTO>> getUserHistory(@PathVariable String userId) {
        log.info("Admin/Librarian viewing entry history for user: {}", userId);
        return ResponseEntity.ok(entryExitService.getUserHistory(userId));
    }
    
    @GetMapping("/stats/daily")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Object>> getDailyStats() {
        return ResponseEntity.ok(entryExitService.getDailyStats());
    }
    
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Long>> getCurrentCount() {
        Map<String, Long> response = new HashMap<>();
        response.put("count", entryExitService.getCurrentCount());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/hours/{branch}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getOpeningHours(@PathVariable String branch) {
        Map<String, Object> response = new HashMap<>();
        response.put("branch", branch);
        response.put("openingHours", libraryHoursService.getOpeningHours(branch));
        response.put("isOpenNow", libraryHoursService.isLibraryOpen(branch, LocalDateTime.now()));
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Entry-Exit Service is healthy!");
    }
}
