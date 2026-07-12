// borrow-service/src/main/java/.../repository/BookLocationRepository.java
package com.university_digital_library.borrow_service.repository;

import com.university_digital_library.borrow_service.model.BookLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookLocationRepository extends JpaRepository<BookLocation, Long> {
    List<BookLocation> findByBookIdAndIsAvailableTrue(Long bookId);
    List<BookLocation> findByBookIdAndIsAvailableFalse(Long bookId);
    List<BookLocation> findByBookId(Long bookId);
}
