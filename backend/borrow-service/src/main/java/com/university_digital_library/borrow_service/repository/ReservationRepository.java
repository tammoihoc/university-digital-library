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

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    // Tìm đặt lịch của user
    List<Reservation> findByUserIdAndStatus(String userId, Reservation.ReservationStatus status);
    List<Reservation> findByUserIdOrderByReservationDateDesc(String userId);
    
    // Tìm đặt lịch theo sách
    List<Reservation> findByBookIdAndStatus(Long bookId, Reservation.ReservationStatus status);
    Page<Reservation> findByBookIdOrderByReservationDateAsc(Long bookId, Pageable pageable);
    
    // Đếm số lượng đặt lịch đang chờ xác nhận cho sách
    long countByBookIdAndStatus(Long bookId, Reservation.ReservationStatus status);
    
    // Tìm đặt lịch cần xử lý
    List<Reservation> findByStatusAndExpiryDateBefore(Reservation.ReservationStatus status, LocalDateTime date);
    
    // Tìm đặt lịch chờ xác nhận
    List<Reservation> findByStatusOrderByReservationDateAsc(Reservation.ReservationStatus status);
    
    // Tìm đặt lịch chờ xác nhận theo sách
    List<Reservation> findByBookIdAndStatusOrderByReservationDateAsc(Long bookId, Reservation.ReservationStatus status);
    
    // Kiểm tra user đã đặt sách này chưa (chưa hoàn thành)
    boolean existsByUserIdAndBookIdAndStatusNot(String userId, Long bookId, Reservation.ReservationStatus excludedStatus);
    
    // Tìm đặt lịch đã xác nhận nhưng chưa đến nhận
    List<Reservation> findByStatusAndPickupDateBefore(Reservation.ReservationStatus status, LocalDateTime date);
    // Thêm method này
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.userId = :userId AND r.status != :status")
    long countByUserIdAndStatusNot(@Param("userId") String userId, @Param("status") Reservation.ReservationStatus status);
}
