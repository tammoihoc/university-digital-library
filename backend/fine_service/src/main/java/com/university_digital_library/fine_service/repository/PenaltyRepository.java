// fine-service/src/main/java/com/university_digital_library/fine_service/repository/PenaltyRepository.java
package com.university_digital_library.fine_service.repository;

import com.university_digital_library.fine_service.model.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, Long> {
    
    List<Penalty> findByUserId(String userId);
    
    List<Penalty> findByIsActiveTrue();
    
    List<Penalty> findByUserIdAndIsActiveTrue(String userId);
}
