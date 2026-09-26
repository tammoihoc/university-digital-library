package com.university_digital_library.entry_exit_service.repository;

import com.university_digital_library.entry_exit_service.model.EntryExitRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntryExitRepository extends JpaRepository<EntryExitRecord, Long> {

    Optional<EntryExitRecord> findFirstByUserIdAndStatusOrderByEntryTimeDesc(String userId, String status);
Optional<EntryExitRecord> findFirstByUserIdAndStatusAndEntryTimeAfterOrderByEntryTimeDesc(
    String userId, String status, LocalDateTime entryTimeAfter
);
    List<EntryExitRecord> findByUserId(String userId);

    List<EntryExitRecord> findByStatus(String status);

    List<EntryExitRecord> findByEntryTimeBetween(LocalDateTime start, LocalDateTime end);

    List<EntryExitRecord> findByUserIdAndStatusAndEntryTimeAfter(String userId, String status, LocalDateTime time);

    boolean existsByUserIdAndStatusAndEntryTimeAfter(String userId, String status, LocalDateTime time);

    boolean existsByUserIdAndStatusAndEntryTimeAfterAndBranch(String userId, String status, LocalDateTime time, String branch);
}
