package com.university_digital_library.book_service.repository;

import com.university_digital_library.book_service.model.BookLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookLocationRepository extends JpaRepository<BookLocation, Long> {
    List<BookLocation> findByBookId(Long bookId);
    List<BookLocation> findByBookIdAndIsAvailableTrue(Long bookId);
    List<BookLocation> findByBookIdAndIsAvailableFalse(Long bookId);
    Optional<BookLocation> findByFullCode(String fullCode);
}
