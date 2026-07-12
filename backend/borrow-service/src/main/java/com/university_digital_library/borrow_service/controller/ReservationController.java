// borrow-service/src/main/java/.../controller/ReservationController.java
package com.university_digital_library.borrow_service.controller;

import com.university_digital_library.borrow_service.dto.ReservationRequest;
import com.university_digital_library.borrow_service.dto.ReservationResponse;
import com.university_digital_library.borrow_service.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {
    
    private final ReservationService reservationService;
    
    // ✅ SỬA - Nhận token từ header và truyền xuống service
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody ReservationRequest request,
            @RequestHeader("Authorization") String authorization,
            Authentication auth) {
        String userId = auth.getName();
        log.info("User {} creating reservation for book {}", userId, request.getBookId());
        return ResponseEntity.ok(reservationService.createReservation(userId, request, authorization));
    }
    
    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(Authentication auth) {
        String userId = auth.getName();
        log.info("Getting reservations for user: {}", userId);
        return ResponseEntity.ok(reservationService.getUserReservations(userId));
    }
    
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable Long reservationId,
            @RequestParam(required = false) String reason,
            @RequestHeader("Authorization") String authorization,
            Authentication auth) {
        String userId = auth.getName();
        log.info("User {} cancelling reservation: {}", userId, reservationId);
        return ResponseEntity.ok(reservationService.cancelReservation(reservationId, userId, reason, authorization));
    }
    
    @PostMapping("/{reservationId}/pickup")
    public ResponseEntity<ReservationResponse> pickupBook(
            @PathVariable Long reservationId,
            @RequestHeader("Authorization") String authorization,
            Authentication auth) {
        String userId = auth.getName();
        log.info("User {} picking up reservation: {}", userId, reservationId);
        return ResponseEntity.ok(reservationService.pickupBook(reservationId, userId, authorization));
    }
    
    // ========== THỦ THƯ/ADMIN ENDPOINTS ==========
    
    @GetMapping("/active/all")
    public ResponseEntity<List<ReservationResponse>> getAllActiveReservations() {
        return ResponseEntity.ok(reservationService.getAllActiveReservations());
    }
    
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ReservationResponse>> getReservationsByBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(reservationService.getReservationsByBook(bookId));
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }
}
