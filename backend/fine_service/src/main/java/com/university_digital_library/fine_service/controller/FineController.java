// fine-service/src/main/java/com/university_digital_library/fine_service/controller/FineController.java
package com.university_digital_library.fine_service.controller;

import com.university_digital_library.fine_service.dto.CreateFineRequest;
import com.university_digital_library.fine_service.dto.FineDTO;
import com.university_digital_library.fine_service.dto.PenaltyRequest;
import com.university_digital_library.fine_service.dto.PenaltyResponse;
import com.university_digital_library.fine_service.service.FineService;
import com.university_digital_library.fine_service.service.PenaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fines")
@RequiredArgsConstructor
@Slf4j
public class FineController {

    private final FineService fineService;
    private final PenaltyService penaltyService;

    // ========== FINE ENDPOINTS ==========

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<FineDTO> createFine(@Valid @RequestBody CreateFineRequest request) {
        FineDTO response = fineService.createFine(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FineDTO>> getUserFines(@PathVariable String userId) {
        List<FineDTO> fines = fineService.getUserFines(userId);
        return ResponseEntity.ok(fines);
    }

    @GetMapping("/user/{userId}/unpaid")
    public ResponseEntity<List<FineDTO>> getUnpaidFines(@PathVariable String userId) {
        List<FineDTO> fines = fineService.getUnpaidFines(userId);
        return ResponseEntity.ok(fines);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<FineDTO>> getOverdueFines() {
        List<FineDTO> fines = fineService.getOverdueFines();
        return ResponseEntity.ok(fines);
    }

    @PutMapping("/{fineId}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<FineDTO> payFine(@PathVariable Long fineId) {
        FineDTO response = fineService.payFine(fineId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{fineId}/waive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FineDTO> waiveFine(@PathVariable Long fineId) {
        // Nếu muốn xóa phạt, có thể gọi payFine hoặc thêm method waive
        FineDTO response = fineService.payFine(fineId); // tạm thời coi như đã thanh toán
        return ResponseEntity.ok(response);
    }

    // ========== PENALTY ENDPOINTS ==========

    @PostMapping("/penalties")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PenaltyResponse> createPenalty(
            @Valid @RequestBody PenaltyRequest request,
            Authentication auth) {

        String createdBy = auth != null ? auth.getName() : "system";
        PenaltyResponse response = penaltyService.createPenalty(request, createdBy);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/penalties/user/{userId}")
    public ResponseEntity<List<PenaltyResponse>> getUserPenalties(@PathVariable String userId) {
        List<PenaltyResponse> penalties = penaltyService.getUserPenalties(userId);
        return ResponseEntity.ok(penalties);
    }

    @GetMapping("/penalties/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<PenaltyResponse>> getActivePenalties() {
        List<PenaltyResponse> penalties = penaltyService.getActivePenalties();
        return ResponseEntity.ok(penalties);
    }

    @GetMapping("/penalties")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<PenaltyResponse>> getAllPenalties() {
        List<PenaltyResponse> penalties = penaltyService.getAllPenalties();
        return ResponseEntity.ok(penalties);
    }

    @PutMapping("/penalties/{penaltyId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PenaltyResponse> resolvePenalty(@PathVariable Long penaltyId) {
        PenaltyResponse response = penaltyService.resolvePenalty(penaltyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Fine Service is healthy! 💰");
    }
}
