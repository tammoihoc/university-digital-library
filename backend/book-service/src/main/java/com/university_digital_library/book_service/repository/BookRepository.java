package com.university_digital_library.book_service.repository;

import com.university_digital_library.book_service.model.Book;
import com.university_digital_library.book_service.model.BookAccessType;
import com.university_digital_library.book_service.model.BookType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    Optional<Book> findByIsbn(String isbn);
    
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);
    
    Page<Book> findByCategoriesContaining(String category, Pageable pageable);
    
    Page<Book> findByType(BookType type, Pageable pageable);
    
    Page<Book> findByAccessType(BookAccessType accessType, Pageable pageable);
    
    Page<Book> findByDepartment(String department, Pageable pageable);
    
    Page<Book> findByCourseCode(String courseCode, Pageable pageable);
    
    List<Book> findByIsActiveTrue();
    
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> fullTextSearch(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE b.isActive = true AND " +
           "(b.accessType = 'FULL_DIGITAL' OR b.accessType = 'HYBRID')")
    List<Book> findAvailableDigitalBooks();
    
    // Thêm method mới cho recommended books
// Thêm method này vào BookRepository interface
Page<Book> findByDepartmentAndIsActiveTrue(String department, Pageable pageable);
    Page<Book> findByLibraryBranchIdAndIsActiveTrue(Long libraryBranchId, Pageable pageable);
}
