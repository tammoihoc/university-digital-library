package com.university_digital_library.borrow_service.repository;

import com.university_digital_library.borrow_service.model.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BorrowRepository extends JpaRepository<BorrowRecord, Long> {

    List<BorrowRecord> findByUserId(String userId);
    List<BorrowRecord> findByBookId(Long bookId);
    List<BorrowRecord> findByUserIdAndReturnedAtIsNull(String userId);
    List<BorrowRecord> findByUserIdAndBookIdAndReturnedAtIsNull(String userId, Long bookId);
    List<BorrowRecord> findByStatus(BorrowRecord.BorrowStatus status);
    List<BorrowRecord> findByDueDateBeforeAndReturnedAtIsNull(LocalDateTime date);
    List<BorrowRecord> findByReservationId(String reservationId); // ✅ THÊM
    long countByUserIdAndReturnedAtIsNull(String userId);

    @Query("SELECT COUNT(b) FROM BorrowRecord b WHERE b.status = :status")
    long countByStatus(@Param("status") BorrowRecord.BorrowStatus status);
}
