// borrow-service/src/main/java/com/university_digital_library/borrow_service/controller/BorrowController.java
package com.university_digital_library.borrow_service.controller;

import com.university_digital_library.borrow_service.dto.BorrowRequest;
import com.university_digital_library.borrow_service.dto.BorrowResponse;
import com.university_digital_library.borrow_service.dto.ReservationRequest;
import com.university_digital_library.borrow_service.dto.ReservationResponse;
import com.university_digital_library.borrow_service.service.BorrowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/borrows")
@RequiredArgsConstructor
@Slf4j
public class BorrowController {
    
    private final BorrowService borrowService;
    
    // ========== BORROW ENDPOINTS ==========
    
@PostMapping
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')") // ✅ thêm security
public ResponseEntity<BorrowResponse> borrowBook(
        @Valid @RequestBody BorrowRequest request,
        @RequestHeader(value = "Authorization", required = false) String authorization,
        Authentication auth) {

    // ✅ LẤY userId TỪ REQUEST (không ghi đè bằng auth.getName())
    String userId = request.getUserId();
    if (userId == null || userId.isBlank()) {
        // Fallback: nếu request không có userId (hiếm), dùng authenticated user
        userId = auth != null ? auth.getName() : null;
    }
    if (userId == null) {
        throw new RuntimeException("User ID is required");
    }
    // Đảm bảo request có userId đúng
    request.setUserId(userId);

    log.info("User {} borrowing book {}", userId, request.getBookId());
    BorrowResponse response = borrowService.borrowBook(request, authorization);
    return ResponseEntity.ok(response);
}
    
    @PostMapping("/{borrowId}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BorrowResponse> returnBook(@PathVariable Long borrowId) {
        log.info("Returning borrow: {}", borrowId);
        BorrowResponse response = borrowService.returnBook(borrowId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<BorrowResponse>> getUserBorrows(@PathVariable String userId) {
        return ResponseEntity.ok(borrowService.getUserBorrows(userId));
    }
    
    @GetMapping("/my")
    public ResponseEntity<List<BorrowResponse>> getMyBorrows(Authentication auth) {
        String userId = auth.getName();
        return ResponseEntity.ok(borrowService.getCurrentBorrows(userId));
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<BorrowResponse>> getActiveBorrows() {
        return ResponseEntity.ok(borrowService.getAllActiveBorrows());
    }
    
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<BorrowResponse>> getOverdueBorrows() {
        return ResponseEntity.ok(borrowService.getOverdueBorrows());
    }
    
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Long>> getActiveBorrowsCount() {
        Map<String, Long> response = new HashMap<>();
        response.put("count", borrowService.getActiveBorrowsCount());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<BorrowResponse>> getBookBorrows(@PathVariable Long bookId) {
        return ResponseEntity.ok(borrowService.getBookBorrows(bookId));
    }
    
    // ========== RESERVATION ENDPOINTS ==========
    @PostMapping("/reservations/{reservationId}/approve")
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
public ResponseEntity<ReservationResponse> approveReservation(
        @PathVariable Long reservationId,
        @RequestHeader(value = "Authorization", required = false) String authorization,
        Authentication auth) {
    String librarianId = auth.getName();
    log.info("Librarian {} approving reservation: {}", librarianId, reservationId);
    ReservationResponse response = borrowService.approveReservation(reservationId, librarianId, authorization);
    return ResponseEntity.ok(response);
}

@PostMapping("/reservations/{reservationId}/reject")
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
public ResponseEntity<ReservationResponse> rejectReservation(
        @PathVariable Long reservationId,
        @RequestParam(required = false) String reason,
        @RequestHeader(value = "Authorization", required = false) String authorization,
        Authentication auth) {
    String librarianId = auth.getName();
    log.info("Librarian {} rejecting reservation: {}", librarianId, reservationId);
    ReservationResponse response = borrowService.rejectReservation(reservationId, reason, librarianId, authorization);
    return ResponseEntity.ok(response);
}
    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            Authentication auth) {
        
        String userId = auth.getName();
        log.info("User {} creating reservation for book {}", userId, request.getBookId());
        ReservationResponse response = borrowService.createReservation(request, userId, authorization);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/all")
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
public ResponseEntity<List<BorrowResponse>> getAllBorrows() {
    return ResponseEntity.ok(borrowService.getAllBorrows());
}
    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable Long reservationId,
            @RequestParam(required = false) String reason,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            Authentication auth) {
        
        String userId = auth.getName();
        log.info("User {} cancelling reservation: {}", userId, reservationId);
        ReservationResponse response = borrowService.cancelReservation(reservationId, userId, reason, authorization);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reservations/{reservationId}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ReservationResponse> confirmReservation(
            @PathVariable Long reservationId,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            Authentication auth) {
        
        String librarianId = auth.getName();
        log.info("Librarian {} confirming reservation: {}", librarianId, reservationId);
        ReservationResponse response = borrowService.confirmReservation(reservationId, librarianId, authorization);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/reservations/my")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(Authentication auth) {
        String userId = auth.getName();
        return ResponseEntity.ok(borrowService.getUserReservations(userId));
    }
    
    @GetMapping("/reservations/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<ReservationResponse>> getActiveReservations() {
        return ResponseEntity.ok(borrowService.getAllActiveReservations());
    }
    
    @GetMapping("/reservations/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(borrowService.getAllReservations());
    }
    
    @GetMapping("/reservations/book/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<ReservationResponse>> getReservationsByBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(borrowService.getReservationsByBook(bookId));
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Borrow Service is healthy! 📚");
    }
}
