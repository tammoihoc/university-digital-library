package com.university_digital_library.borrow_service.service;

import com.university_digital_library.borrow_service.dto.BorrowRequest;
import com.university_digital_library.borrow_service.dto.BorrowResponse;
import java.util.List;

public interface BorrowService {
    
    BorrowResponse borrowBook(BorrowRequest request, String authorization);
    
    BorrowResponse returnBook(Long borrowId);
    
    List<BorrowResponse> getUserBorrows(String userId);
    
    List<BorrowResponse> getCurrentBorrows(String userId);
    
    List<BorrowResponse> getOverdueBorrows();
    
    List<BorrowResponse> getBookBorrows(Long bookId);
    
    List<BorrowResponse> getAllActiveBorrows();
    
    long getActiveBorrowsCount();
}
