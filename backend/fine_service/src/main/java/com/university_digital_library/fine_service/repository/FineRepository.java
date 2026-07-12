// /home/tam/university-digital-library/backend/fine-service/src/main/java/com/university_digital_library/fine_service/repository/FineRepository.java
package com.university_digital_library.fine_service.repository;

import com.university_digital_library.fine_service.model.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    
    List<Fine> findByUserId(String userId);
    
    List<Fine> findByIsPaidFalse();
    
    List<Fine> findByIsPaidTrue();
    
    List<Fine> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f WHERE f.isPaid = false")
    Double sumUnpaidFines();
    
    long countByIsPaidFalse();
}
