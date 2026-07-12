// borrow-service/src/main/java/.../controller/BorrowController.java
package com.university_digital_library.borrow_service.controller;

import com.university_digital_library.borrow_service.dto.BorrowRequest;
import com.university_digital_library.borrow_service.dto.BorrowResponse;
import com.university_digital_library.borrow_service.dto.DirectBorrowRequest;
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
    // KHÔNG cần JwtUtil ở đây
    
    @GetMapping("/test")
    public String test() {
        return "Borrow Controller is working!";
    }
    
    @PostMapping("/direct")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BorrowResponse> directBorrowBook(
            @Valid @RequestBody DirectBorrowRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            Authentication auth) {
        
        String librarianId = auth != null ? auth.getName() : "system";
        log.info("Librarian {} is creating direct borrow for user: {}, book: {}", 
                 librarianId, request.getUserId(), request.getBookId());
        
        // Không cần tạo token mới, chỉ cần truyền authorization xuống service
        // Feign interceptor sẽ tự động thêm token vào header khi gọi service khác
        
        BorrowRequest borrowRequest = new BorrowRequest();
        borrowRequest.setUserId(request.getUserId());
        borrowRequest.setBookId(request.getBookId());
        borrowRequest.setNotes(request.getNotes());
        
        BorrowResponse response = borrowService.borrowBook(borrowRequest, authorization);
        return ResponseEntity.ok(response);
    }

    
    @PostMapping("/{borrowId}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BorrowResponse> returnBook(@PathVariable Long borrowId) {
        BorrowResponse response = borrowService.returnBook(borrowId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<BorrowResponse>> getUserBorrows(@PathVariable String userId) {
        List<BorrowResponse> response = borrowService.getUserBorrows(userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/my/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BorrowResponse>> getMyCurrentBorrows(Authentication auth) {
        String userId = auth.getName();
        List<BorrowResponse> response = borrowService.getCurrentBorrows(userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}/current")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<BorrowResponse>> getCurrentBorrows(@PathVariable String userId) {
        List<BorrowResponse> response = borrowService.getCurrentBorrows(userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<BorrowResponse>> getBookBorrows(@PathVariable Long bookId) {
        List<BorrowResponse> response = borrowService.getBookBorrows(bookId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<BorrowResponse>> getOverdueBorrows() {
        List<BorrowResponse> response = borrowService.getOverdueBorrows();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<BorrowResponse>> getAllActiveBorrows() {
        List<BorrowResponse> response = borrowService.getAllActiveBorrows();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/active/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Long>> getActiveBorrowsCount() {
        Map<String, Long> response = new HashMap<>();
        response.put("count", borrowService.getActiveBorrowsCount());
        return ResponseEntity.ok(response);
    }
}
