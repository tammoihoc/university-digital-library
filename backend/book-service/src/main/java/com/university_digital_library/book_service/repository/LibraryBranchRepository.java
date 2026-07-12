// book-service/src/main/java/.../repository/LibraryBranchRepository.java
package com.university_digital_library.book_service.repository;

import com.university_digital_library.book_service.model.LibraryBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LibraryBranchRepository extends JpaRepository<LibraryBranch, Long> {
    
    List<LibraryBranch> findByIsActiveTrue();
    
    Optional<LibraryBranch> findByName(String name);
}
