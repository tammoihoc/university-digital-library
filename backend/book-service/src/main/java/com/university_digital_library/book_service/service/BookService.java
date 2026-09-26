// book-service/src/main/java/com/university_digital_library/book_service/service/BookService.java
package com.university_digital_library.book_service.service;

import com.university_digital_library.book_service.dto.BookDTO;
import com.university_digital_library.book_service.dto.CreateBookRequest;
import com.university_digital_library.book_service.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {
    
    BookDTO createBook(CreateBookRequest request);
    BookDTO getBookById(Long id);
    Page<BookDTO> getAllBooks(Pageable pageable);
    Page<BookDTO> searchBooks(String keyword, Pageable pageable);
    Page<BookDTO> getBooksByCategory(String category, Pageable pageable);
    Page<BookDTO> getBooksByDepartment(String department, Pageable pageable);
    BookDTO updateBook(Long id, CreateBookRequest request);
    void deleteBook(Long id);
    
    String uploadCoverImage(Long id, MultipartFile file);
    String uploadPdfFile(Long id, MultipartFile file);
    BookDTO updateAvailableCopies(Long id, Integer newAvailable);

    // Tăng/giảm nguyên tử ở DB, tránh race condition khi nhiều request mượn/trả cùng lúc.
    // Trả về true nếu thao tác thành công, false nếu không còn bản sao nào để giảm.
    boolean decrementAvailableCopies(Long id);
    boolean incrementAvailableCopies(Long id);
    
    Book getBookEntity(Long id);  // ✅ ĐÃ CÓ, KHÔNG CẦN SỬA
    boolean canUserAccessBook(Long bookId, String userType, String department);
    Integer countPdfPages(Long bookId);
}
