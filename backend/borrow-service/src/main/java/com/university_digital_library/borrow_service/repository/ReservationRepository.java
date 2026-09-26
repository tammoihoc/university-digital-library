// borrow-service/src/main/java/com/university_digital_library/borrow_service/repository/ReservationRepository.java
package com.university_digital_library.borrow_service.repository;

import com.university_digital_library.borrow_service.model.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    // ========== BASIC QUERIES ==========
    List<Reservation> findByStatus(Reservation.ReservationStatus status);
    
    List<Reservation> findByUserIdOrderByReservationDateDesc(String userId);
    
    List<Reservation> findByBookId(Long bookId);
    
    List<Reservation> findByBookIdAndStatus(Long bookId, Reservation.ReservationStatus status);
    
    List<Reservation> findByStatusAndExpiryDateBefore(Reservation.ReservationStatus status, LocalDateTime date);
    
    // ========== COUNT QUERIES ==========
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.userId = :userId AND r.status != :status")
    long countByUserIdAndStatusNot(@Param("userId") String userId, @Param("status") Reservation.ReservationStatus status);
    
    // ========== EXISTS QUERIES ==========
    boolean existsByUserIdAndBookIdAndStatusNot(String userId, Long bookId, Reservation.ReservationStatus status);
    
    // ========== ORDERED QUERIES ==========
    List<Reservation> findByStatusOrderByReservationDateAsc(Reservation.ReservationStatus status);
    
    List<Reservation> findByBookIdAndStatusOrderByReservationDateAsc(Long bookId, Reservation.ReservationStatus status);
    
    // ========== PAGEABLE QUERIES ==========
    Page<Reservation> findByBookIdOrderByReservationDateAsc(Long bookId, Pageable pageable);
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END FROM Reservation r " +
       "WHERE r.userId = :userId AND r.bookId = :bookId AND r.status IN :statuses")
boolean existsByUserIdAndBookIdAndStatusIn(
    @Param("userId") String userId,
    @Param("bookId") Long bookId,
    @Param("statuses") List<Reservation.ReservationStatus> statuses
);
    // ========== EXPIRED CHECK ==========
    List<Reservation> findByStatusAndPickupDateBefore(Reservation.ReservationStatus status, LocalDateTime date);
}
