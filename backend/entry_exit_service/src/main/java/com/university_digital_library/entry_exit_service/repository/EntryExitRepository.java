// entry-exit-service/src/main/java/.../repository/EntryExitRepository.java
package com.university_digital_library.entry_exit_service.repository;

import com.university_digital_library.entry_exit_service.model.EntryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EntryExitRepository extends JpaRepository<EntryRecord, Long> {
    
    List<EntryRecord> findByUserIdOrderByEntryTimeDesc(String userId);
    
    List<EntryRecord> findByUserIdAndBranch(String userId, String branch);
    
    @Query("SELECT e FROM EntryRecord e WHERE e.entryTime BETWEEN :start AND :end ORDER BY e.entryTime DESC")
    List<EntryRecord> findByEntryTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // ✅ CHỈ GIỮ 1 METHOD NÀY, XÓA METHOD KIA
    @Query("SELECT e FROM EntryRecord e WHERE e.userId = :userId AND e.branch = :branch AND e.entryTime BETWEEN :start AND :end")
    List<EntryRecord> findByUserIdAndBranchAndEntryTimeBetween(
            @Param("userId") String userId, 
            @Param("branch") String branch,
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);
    
    long countByEntryTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(e) FROM EntryRecord e WHERE DATE(e.entryTime) = CURRENT_DATE")
    long countTodayEntries();
}
