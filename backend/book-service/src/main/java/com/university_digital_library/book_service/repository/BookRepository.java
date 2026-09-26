package com.university_digital_library.book_service.repository;

import com.university_digital_library.book_service.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    Optional<Book> findByIsbn(String isbn);

    // Trừ 1 bản sao có sẵn NGAY TẠI DATABASE (atomic UPDATE), thay vì
    // đọc availableCopies rồi tính toán ở Java rồi ghi lại (read-modify-write) —
    // cách cũ có race condition: 2 request đọc cùng lúc availableCopies=1,
    // cả 2 đều tính ra 0 rồi ghi đè, sách bị "bán" 2 lần cho 2 người mượn.
    // Điều kiện "availablePhysicalCopies > 0" trong WHERE đảm bảo không bao giờ
    // giảm xuống âm dù có nhiều request đồng thời.
    // Trả về số dòng bị ảnh hưởng: 1 = thành công, 0 = hết sách (request khác đã lấy mất).
    @Modifying
    @Transactional
    @Query("UPDATE Book b SET b.availablePhysicalCopies = b.availablePhysicalCopies - 1 " +
           "WHERE b.id = :id AND b.availablePhysicalCopies > 0")
    int decrementAvailableCopies(@Param("id") Long id);

    // Cộng lại 1 bản sao (khi trả sách / hủy đặt trước / hết hạn đặt trước),
    // giới hạn không vượt quá totalPhysicalCopies để tránh lệch số liệu nếu bị gọi trùng.
    @Modifying
    @Transactional
    @Query("UPDATE Book b SET b.availablePhysicalCopies = b.availablePhysicalCopies + 1 " +
           "WHERE b.id = :id AND b.availablePhysicalCopies < b.totalPhysicalCopies")
    int incrementAvailableCopies(@Param("id") Long id);
    
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);
    
    Page<Book> findByCategoriesContaining(String category, Pageable pageable);
    
    Page<Book> findByDepartment(String department, Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> fullTextSearch(@Param("keyword") String keyword, Pageable pageable);
}
