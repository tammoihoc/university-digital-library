package com.university_digital_library.borrow_service.service.impl;

import com.university_digital_library.borrow_service.dto.BorrowRequest;
import com.university_digital_library.borrow_service.dto.BorrowResponse;
import com.university_digital_library.borrow_service.dto.EntryResponseDTO;
import com.university_digital_library.borrow_service.feign.BookClient;
import com.university_digital_library.borrow_service.feign.EntryExitClient;
import com.university_digital_library.borrow_service.feign.UserClient;
import com.university_digital_library.borrow_service.model.BorrowRecord;
import com.university_digital_library.borrow_service.model.BookLocation;
import com.university_digital_library.borrow_service.repository.BorrowRepository;
import com.university_digital_library.borrow_service.repository.BookLocationRepository;
import com.university_digital_library.borrow_service.service.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowServiceImpl implements BorrowService {
    
    private final BorrowRepository borrowRepository;
    private final BookClient bookClient;
    private final UserClient userClient;
    private final EntryExitClient entryExitClient;
    private final BookLocationRepository bookLocationRepository;  // ✅ THÊM DÒNG NÀY

    @Override
    @Transactional
    public BorrowResponse borrowBook(BorrowRequest request, String authorization) {
        log.info("Borrowing book {} for user {}", request.getBookId(), request.getUserId());
        
        try {
            // 1. KIỂM TRA USER ĐÃ CHECK-IN TRONG NGÀY CHƯA
List<EntryResponseDTO> currentEntries = entryExitClient.getCurrentEntries(null);
            
            boolean isCheckedIn = currentEntries.stream()
                    .anyMatch(entry -> entry.getUserId().equals(request.getUserId()));
            
            if (!isCheckedIn) {
                throw new RuntimeException("Bạn chưa check-in vào thư viện hôm nay! Vui lòng quét thẻ tại cửa trước khi mượn sách.");
            }
            
            String checkedInBranch = currentEntries.stream()
                    .filter(entry -> entry.getUserId().equals(request.getUserId()))
                    .findFirst()
                    .map(EntryResponseDTO::getBranch)
                    .orElse("B");
            
            log.info("User {} checked in at branch: {}", request.getUserId(), checkedInBranch);
            
            // 2. KIỂM TRA HẠN MỨC MƯỢN
            Map<String, Object> userInfo = userClient.getUserBorrowInfo(request.getUserId(), authorization);
            
            Integer maxBorrow = (Integer) userInfo.get("maxBorrowLimit");
            Integer currentBorrowed = (Integer) userInfo.get("currentBorrowed");
            
            if (currentBorrowed != null && maxBorrow != null && currentBorrowed >= maxBorrow) {
                throw new RuntimeException("Bạn đã đạt giới hạn mượn sách (" + maxBorrow + " cuốn)");
            }
            
            // 3. KIỂM TRA SÁCH CÓ SẴN VÀ LẤY VỊ TRÍ
            Map<String, Object> bookInfo = bookClient.getBookAvailability(request.getBookId(), authorization);
            
            Integer availableCopies = (Integer) bookInfo.get("availablePhysicalCopies");
            Boolean canBeBorrowed = (Boolean) bookInfo.get("canBeBorrowed");
            
            if (canBeBorrowed == null || !canBeBorrowed || availableCopies == null || availableCopies <= 0) {
                throw new RuntimeException("Sách hiện không có sẵn để mượn");
            }
            
            Map<String, Object> locationInfo = bookClient.getBookLocation(request.getBookId(), authorization);
            String bookLocation = (String) locationInfo.getOrDefault("fullCode", "Chưa có vị trí");
            String bookBranch = (String) locationInfo.getOrDefault("building", "B");
            
            // 4. KIỂM TRA ĐÚNG KHU
            if (!checkedInBranch.equals(bookBranch)) {
                throw new RuntimeException("Sách này đang ở khu " + bookBranch + ", bạn cần check-in tại khu " + bookBranch + " để mượn!");
            }
            
            // 5. TẠO RECORD MƯỢN SÁCH
            BorrowRecord record = new BorrowRecord();
            record.setUserId(request.getUserId());
            record.setBookId(request.getBookId());
            record.setNotes("📍 Vị trí: " + bookLocation + " (Khu " + bookBranch + ") - Mượn tại khu " + checkedInBranch);
            
            BorrowRecord saved = borrowRepository.save(record);
            log.info("Created borrow record: {} - Location: {}", saved.getId(), bookLocation);
            
            // 6. CẬP NHẬT SỐ LƯỢNG SÁCH ĐANG MƯỢN CỦA USER
            if (currentBorrowed != null) {
                userClient.updateBorrowCount(request.getUserId(), currentBorrowed + 1, authorization);
            }
            
            // 7. GIẢM SỐ LƯỢNG SÁCH CÓ SẴN
            if (availableCopies != null) {
                bookClient.updateAvailableCopies(request.getBookId(), availableCopies - 1, authorization);
                log.info("Decreased available copies for book {} from {} to {}", 
                         request.getBookId(), availableCopies, availableCopies - 1);
            }
            
            // ✅ 8. ẨN VỊ TRÍ CỦA BẢN SÁCH VỪA MƯỢN
            List<BookLocation> availableLocations = bookLocationRepository.findByBookIdAndIsAvailableTrue(request.getBookId());
            if (!availableLocations.isEmpty()) {
                BookLocation borrowedLocation = availableLocations.get(0);
                borrowedLocation.setIsAvailable(false);
                bookLocationRepository.save(borrowedLocation);
                log.info("🔒 Hidden location {} for book {} (copy {})", 
                         borrowedLocation.getFullCode(), request.getBookId(), borrowedLocation.getCopyNumber());
            }
            
            BorrowResponse response = BorrowResponse.fromEntity(saved);
            response.setNotes("📍 Đã mượn sách tại khu " + checkedInBranch + " - Vị trí: " + bookLocation);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error borrowing book: {}", e.getMessage());
            throw new RuntimeException("Không thể mượn sách: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public BorrowResponse returnBook(Long borrowId) {
        log.info("Returning borrow record: {}", borrowId);
        
        BorrowRecord record = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new RuntimeException("Borrow record not found"));
        
        if (record.getReturnedAt() != null) {
            throw new RuntimeException("Book already returned");
        }
        
        // 1. CẬP NHẬT RECORD TRẢ SÁCH
        record.setReturnedAt(LocalDateTime.now());
        record.setStatus(BorrowRecord.BorrowStatus.RETURNED);
        
        // 2. TÍNH TIỀN PHẠT NẾU QUÁ HẠN
        if (LocalDateTime.now().isAfter(record.getDueDate())) {
            long overdueDays = java.time.Duration.between(record.getDueDate(), LocalDateTime.now()).toDays();
            double fine = overdueDays * 5000;
            record.setFineAmount(fine);
            log.warn("Overdue fine: {} VND for {} days", fine, overdueDays);
        }
        
        BorrowRecord saved = borrowRepository.save(record);
        
        // 3. TĂNG LẠI SỐ LƯỢNG SÁCH CÓ SẴN
        try {
            Map<String, Object> bookInfo = bookClient.getBookAvailability(record.getBookId(), null);
            Integer availableCopies = (Integer) bookInfo.get("availablePhysicalCopies");
            Integer totalCopies = (Integer) bookInfo.get("totalPhysicalCopies");
            
            if (availableCopies != null && availableCopies < totalCopies) {
                int newAvailable = availableCopies + 1;
                bookClient.updateAvailableCopies(record.getBookId(), newAvailable, null);
                log.info("✅ Increased available copies for book {} from {} to {}", 
                         record.getBookId(), availableCopies, newAvailable);
            }
        } catch (Exception e) {
            log.error("Error updating available copies: {}", e.getMessage());
        }
        
        // 4. GIẢM SỐ LƯỢNG SÁCH ĐANG MƯỢN CỦA USER
        try {
            Map<String, Object> userInfo = userClient.getUserBorrowInfo(record.getUserId(), null);
            Integer currentBorrowed = (Integer) userInfo.get("currentBorrowed");
            if (currentBorrowed != null && currentBorrowed > 0) {
                userClient.updateBorrowCount(record.getUserId(), currentBorrowed - 1, null);
                log.info("✅ Decreased borrow count for user {} from {} to {}", 
                         record.getUserId(), currentBorrowed, currentBorrowed - 1);
            }
        } catch (Exception e) {
            log.error("Error updating user borrow count: {}", e.getMessage());
        }
        
        // ✅ 5. HIỆN LẠI VỊ TRÍ CỦA BẢN SÁCH VỪA TRẢ
        List<BookLocation> borrowedLocations = bookLocationRepository.findByBookIdAndIsAvailableFalse(record.getBookId());
        if (!borrowedLocations.isEmpty()) {
            BookLocation returnedLocation = borrowedLocations.get(0);
            returnedLocation.setIsAvailable(true);
            bookLocationRepository.save(returnedLocation);
            log.info("🔓 Revealed location {} for book {} (copy {})", 
                     returnedLocation.getFullCode(), record.getBookId(), returnedLocation.getCopyNumber());
        }
        
        return BorrowResponse.fromEntity(saved);
    }
    
    @Override
    public List<BorrowResponse> getUserBorrows(String userId) {
        return borrowRepository.findByUserId(userId).stream()
                .map(BorrowResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BorrowResponse> getCurrentBorrows(String userId) {
        return borrowRepository.findByUserIdAndReturnedAtIsNull(userId).stream()
                .map(BorrowResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BorrowResponse> getOverdueBorrows() {
        return borrowRepository.findByDueDateBeforeAndReturnedAtIsNull(LocalDateTime.now()).stream()
                .map(BorrowResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BorrowResponse> getBookBorrows(Long bookId) {
        return borrowRepository.findByBookId(bookId).stream()
                .map(BorrowResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BorrowResponse> getAllActiveBorrows() {
        log.info("Getting all active borrows");
        List<BorrowRecord> activeBorrows = borrowRepository.findByStatus(BorrowRecord.BorrowStatus.ACTIVE);
        List<BorrowRecord> overdueBorrows = borrowRepository.findByStatus(BorrowRecord.BorrowStatus.OVERDUE);
        
        List<BorrowRecord> allActive = new ArrayList<>();
        allActive.addAll(activeBorrows);
        allActive.addAll(overdueBorrows);
        
        return allActive.stream()
                .map(BorrowResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public long getActiveBorrowsCount() {
        long activeCount = borrowRepository.countByStatus(BorrowRecord.BorrowStatus.ACTIVE);
        long overdueCount = borrowRepository.countByStatus(BorrowRecord.BorrowStatus.OVERDUE);
        return activeCount + overdueCount;
    }
}
