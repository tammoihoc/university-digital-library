package com.university_digital_library.entry_exit_service.controller;

import com.university_digital_library.entry_exit_service.dto.EntryExitRequest;
import com.university_digital_library.entry_exit_service.dto.EntryExitResponse;
import com.university_digital_library.entry_exit_service.model.EntryExitRecord;
import com.university_digital_library.entry_exit_service.service.EntryExitService;
import jakarta.validation.Valid;
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
import java.util.Optional;

@RestController
@RequestMapping("/entry-exit")
@RequiredArgsConstructor
@Slf4j
public class EntryExitController {

    private final EntryExitService entryExitService;

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<EntryExitResponse> checkIn(
            @Valid @RequestBody EntryExitRequest request,
            Authentication auth,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        // Lấy identifier từ request (có thể là username hoặc studentId)
        String identifier = request.getUserId();
        if (identifier == null || identifier.isBlank()) {
            identifier = auth != null ? auth.getName() : null;
        }
        if (identifier == null) {
            throw new RuntimeException("User ID is required");
        }

        log.info("User {} checking in at branch {}", identifier, request.getBranch());
        EntryExitResponse response = entryExitService.checkIn(identifier, request, authorization);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<EntryExitResponse> checkOut(
            @RequestBody EntryExitRequest request,
            Authentication auth) {

        String userId = request.getUserId();
        if (userId == null || userId.isBlank()) {
            userId = auth != null ? auth.getName() : null;
        }
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        log.info("User {} checking out", userId);
        EntryExitResponse response = entryExitService.checkOut(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<EntryExitResponse>> getCurrentEntries() {
        return ResponseEntity.ok(entryExitService.getCurrentEntries());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<EntryExitResponse>> getUserHistory(@PathVariable String userId) {
        return ResponseEntity.ok(entryExitService.getUserHistory(userId));
    }

    @GetMapping("/user/{userId}/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Boolean> isUserCheckedInToday(@PathVariable String userId) {
        return ResponseEntity.ok(entryExitService.isUserCheckedInToday(userId));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<EntryExitResponse>> getHistory(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate + "T00:00:00") : LocalDateTime.now().minusDays(7);
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate + "T23:59:59") : LocalDateTime.now();

        List<EntryExitResponse> history = entryExitService.getHistory(start, end);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/user/{userId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Object>> getUserStatus(@PathVariable String userId) {
        Optional<EntryExitRecord> latest = entryExitService.getLatestEntry(userId);
        if (latest.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "OUTSIDE");
            response.put("checkedIn", false);
            response.put("branch", null);
            response.put("entryTime", null);
            return ResponseEntity.ok(response);
        }
        EntryExitRecord record = latest.get();
        Map<String, Object> response = new HashMap<>();
        response.put("status", record.getStatus());
        response.put("checkedIn", "INSIDE".equals(record.getStatus()));
        response.put("branch", record.getBranch());
        response.put("entryTime", record.getEntryTime());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Entry-Exit Service is healthy! 🚪");
    }
}
