// book-service/src/main/java/.../service/BookService.java
package com.university_digital_library.book_service.service;

import com.university_digital_library.book_service.dto.request.CreateBookRequest;
import com.university_digital_library.book_service.dto.response.BookResponse;
import com.university_digital_library.book_service.model.Book;
import com.university_digital_library.book_service.model.BookLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.university_digital_library.book_service.model.LibraryBranch;
import java.util.List;  // ✅ THÊM IMPORT NÀY

public interface BookService {
    
    // Basic CRUD
    BookResponse createBook(CreateBookRequest request);
    BookResponse getBookById(Long id);
    BookResponse getBookWithUserAccess(Long bookId, String username, String userType, String department);
    Page<BookResponse> getAllBooks(Pageable pageable);
    Page<BookResponse> searchBooks(String keyword, Pageable pageable);
    Page<BookResponse> getBooksByCategory(String category, Pageable pageable);
    Page<BookResponse> getBooksByType(String type, Pageable pageable);
    Page<BookResponse> getBooksByDepartment(String department, Pageable pageable);
    Page<BookResponse> getRecommendedBooks(String userType, String department, Pageable pageable);
    BookResponse updateBook(Long id, CreateBookRequest request);
    void deleteBook(Long id);
    
    // File upload
    String uploadCoverImage(Long bookId, MultipartFile file);
    String uploadPdfFile(Long bookId, MultipartFile file);
    BookResponse updateBookAccessType(Long bookId, String accessType);
    
    // PDF Metadata
    BookResponse uploadPdfWithMetadata(Long bookId, MultipartFile file);
    int updateAllBookPages();
    BookResponse updateBookPagesFromPdf(Long bookId);
    
    // Location management - THÊM CÁC METHOD NÀY
    List<BookLocation> searchByLocation(Integer zone, Integer shelf, Integer column, Integer row);
    BookLocation getBookLocation(Long bookId);
    
    // Helper methods
    boolean canUserAccessBook(Book book, String userType, String department, String courseCode);
    Book getBookEntityById(Long id);
    Book saveBook(Book book);
        // Library Branch methods
    List<LibraryBranch> getAllBranches();
    LibraryBranch getBranchById(Long id);
    Page<BookResponse> getBooksByBranch(Long branchId, Pageable pageable);
    
List<Book> getAllBooksEntity();
    // Location methods
    List<String> getAvailableLocations(Long bookId, Long branchId);
}
