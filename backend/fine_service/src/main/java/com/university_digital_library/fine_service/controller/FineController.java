package com.university_digital_library.fine_service.controller;

import com.university_digital_library.fine_service.dto.CreateFineRequest;
import com.university_digital_library.fine_service.dto.FineDTO;
import com.university_digital_library.fine_service.dto.ReportDamageRequest;
import com.university_digital_library.fine_service.service.FineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fines")
@RequiredArgsConstructor
@Slf4j
public class FineController {
    
    private final FineService fineService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<FineDTO>> getAllFines() {
        return ResponseEntity.ok(fineService.getAllFines());
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<FineDTO>> getUserFines(@PathVariable String userId) {
        return ResponseEntity.ok(fineService.getUserFines(userId));
    }
    
    @GetMapping("/unpaid")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<FineDTO>> getUnpaidFines() {
        return ResponseEntity.ok(fineService.getUnpaidFines());
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<FineDTO> createFine(@Valid @RequestBody CreateFineRequest request) {
        return ResponseEntity.ok(fineService.createFine(request));
    }
    
    @PatchMapping("/{fineId}/paid")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<FineDTO> markAsPaid(@PathVariable Long fineId) {
        return ResponseEntity.ok(fineService.markAsPaid(fineId));
    }
    
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Object>> getFineStats() {
        return ResponseEntity.ok(fineService.getFineStats());
    }
    
    @GetMapping("/total-unpaid")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Double>> getTotalUnpaid() {
        Map<String, Double> response = new HashMap<>();
        response.put("totalUnpaid", fineService.getTotalUnpaidAmount());
        return ResponseEntity.ok(response);
    }
    
    // API báo cáo mất/hư sách
    @PostMapping("/report-loss")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<FineDTO> reportLoss(@RequestBody ReportDamageRequest request) {
        log.info("Reporting loss for borrow record: {}", request.getBorrowId());
        FineDTO fine = fineService.reportLoss(request);
        return ResponseEntity.ok(fine);
    }
    
    @PostMapping("/report-damage")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<FineDTO> reportDamage(@RequestBody ReportDamageRequest request) {
        log.info("Reporting damage for borrow record: {}", request.getBorrowId());
        FineDTO fine = fineService.reportDamage(request);
        return ResponseEntity.ok(fine);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Fine Service is healthy!");
    }
}
