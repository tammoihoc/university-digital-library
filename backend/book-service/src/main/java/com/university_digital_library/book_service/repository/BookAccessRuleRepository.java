package com.university_digital_library.book_service.repository;

import com.university_digital_library.book_service.model.BookAccessRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookAccessRuleRepository extends JpaRepository<BookAccessRule, Long> {
    
    List<BookAccessRule> findByBookId(Long bookId);
    
    List<BookAccessRule> findByUserType(String userType);
    
    List<BookAccessRule> findByDepartment(String department);
    //Page<Book> findByLibraryBranchIdAndIsActiveTrue(Long libraryBranchId, Pageable pageable);
    List<BookAccessRule> findByCourseCode(String courseCode);
}
