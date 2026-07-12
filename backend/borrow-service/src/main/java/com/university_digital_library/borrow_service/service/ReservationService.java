// borrow-service/src/main/java/.../service/ReservationService.java
package com.university_digital_library.borrow_service.service;

import com.university_digital_library.borrow_service.dto.BorrowRequest;
import com.university_digital_library.borrow_service.dto.ReservationRequest;
import com.university_digital_library.borrow_service.dto.ReservationResponse;
import com.university_digital_library.borrow_service.feign.BookClient;
import com.university_digital_library.borrow_service.model.Reservation;
import com.university_digital_library.borrow_service.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final BookClient bookClient;
    private final BorrowService borrowService;
    
    private static final int RESERVATION_HOLD_DAYS = 7;
    private static final int MAX_RESERVATIONS_PER_USER = 5;
    
// borrow-service/src/main/java/.../service/ReservationService.java
// Thêm logic ẩn vị trí khi đặt trước

@Transactional
public ReservationResponse createReservation(String userId, ReservationRequest request, String authorization) {
    log.info("User {} is reserving book {}", userId, request.getBookId());
    
    try {
        long activeReservationsCount = reservationRepository.countByUserIdAndStatusNot(
            userId, Reservation.ReservationStatus.CANCELLED);
        
        if (activeReservationsCount >= MAX_RESERVATIONS_PER_USER) {
            throw new RuntimeException("Bạn chỉ có thể đặt tối đa " + MAX_RESERVATIONS_PER_USER + " cuốn sách cùng lúc");
        }
        
        // Lấy thông tin sách và vị trí
        Map<String, Object> bookInfo = bookClient.getBookAvailability(request.getBookId(), authorization);
        
        if (bookInfo == null || bookInfo.get("error") != null) {
            throw new RuntimeException("Không tìm thấy sách");
        }
        
        Integer availableCopies = (Integer) bookInfo.get("availablePhysicalCopies");
        Boolean canBeBorrowed = (Boolean) bookInfo.get("canBeBorrowed");
        
        if (canBeBorrowed == null || !canBeBorrowed || availableCopies == null || availableCopies <= 0) {
            throw new RuntimeException("Sách hiện không có sẵn để đặt");
        }
        
        // Lấy vị trí sách
        Map<String, Object> locationInfo = bookClient.getBookLocation(request.getBookId(), authorization);
        String bookLocation = (String) locationInfo.getOrDefault("fullCode", "Chưa có vị trí");
        String branch = (String) locationInfo.getOrDefault("building", "B");
        
        // Kiểm tra user đã đặt sách này chưa
        if (reservationRepository.existsByUserIdAndBookIdAndStatusNot(userId, request.getBookId(), 
                Reservation.ReservationStatus.CANCELLED)) {
            throw new RuntimeException("Bạn đã đặt lịch cho sách này rồi");
        }
        
        LocalDateTime pickupDate = LocalDateTime.now().plusDays(1);
        LocalDateTime expiryDate = pickupDate.plusDays(RESERVATION_HOLD_DAYS);
        
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setBookId(request.getBookId());
        reservation.setPickupDate(pickupDate);
        reservation.setExpiryDate(expiryDate);
        reservation.setNotes("📍 Vị trí dự kiến: " + bookLocation + " (Tòa " + branch + ") - Đã đặt trước, vị trí tạm ẩn");
        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        reservation.setConfirmedAt(LocalDateTime.now());
        reservation.setConfirmedBy("SYSTEM");
        reservation.setReservationDate(LocalDateTime.now());
        
        Reservation saved = reservationRepository.save(reservation);
        log.info("Reservation created: {} - Location {} temporarily hidden", saved.getId(), bookLocation);
        
        // GIẢM SỐ LƯỢNG SÁCH CÓ SẴN (ẨN VỊ TRÍ ĐÓ ĐI)
        bookClient.updateAvailableCopies(request.getBookId(), availableCopies - 1, authorization);
        log.info("Book {} available copies decreased from {} to {} (reservation)", 
                 request.getBookId(), availableCopies, availableCopies - 1);
        
        ReservationResponse response = ReservationResponse.fromEntity(saved);
        response.setNotes("📍 Đã đặt trước sách tại khu " + branch + " - Vị trí: " + bookLocation + " (tạm ẩn cho đến khi bạn đến nhận)");
        
        return response;
        
    } catch (Exception e) {
        log.error("Error creating reservation: {}", e.getMessage());
        throw new RuntimeException(e.getMessage());
    }
}

@Transactional
public ReservationResponse cancelReservation(Long reservationId, String userId, String reason, String authorization) {
    Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt lịch"));
    
    if (!reservation.getUserId().equals(userId)) {
        throw new RuntimeException("Bạn chỉ có thể hủy đặt lịch của chính mình");
    }
    
    if (reservation.getStatus() == Reservation.ReservationStatus.COMPLETED) {
        throw new RuntimeException("Không thể hủy đặt lịch đã hoàn thành");
    }
    
    // Nếu đặt lịch đang ở trạng thái CONFIRMED, trả lại số lượng sách (HIỆN LẠI VỊ TRÍ)
    if (reservation.getStatus() == Reservation.ReservationStatus.CONFIRMED) {
        try {
            Map<String, Object> bookInfo = bookClient.getBookAvailability(reservation.getBookId(), authorization);
            Integer availableCopies = (Integer) bookInfo.get("availablePhysicalCopies");
            if (availableCopies != null) {
                bookClient.updateAvailableCopies(reservation.getBookId(), availableCopies + 1, authorization);
                log.info("Đã trả lại 1 bản sách cho book {} sau khi hủy (vị trí hiện lại)", reservation.getBookId());
            }
        } catch (Exception e) {
            log.error("Lỗi khi trả lại sách: {}", e.getMessage());
        }
    }
    
    reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
    reservation.setCancelledAt(LocalDateTime.now());
    reservation.setCancelReason(reason != null ? reason : "Người dùng hủy");
    
    Reservation saved = reservationRepository.save(reservation);
    log.info("Reservation cancelled: {} - Location is now available again", saved.getId());
    
    return ReservationResponse.fromEntity(saved);
}
    @Transactional
    public ReservationResponse pickupBook(Long reservationId, String userId, String authorization) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        
        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("This reservation belongs to another user");
        }
        
        if (reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw new RuntimeException("Reservation is not confirmed or already expired");
        }
        
        if (LocalDateTime.now().isAfter(reservation.getExpiryDate())) {
            reservation.setStatus(Reservation.ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
            throw new RuntimeException("Reservation has expired. Please make a new reservation.");
        }
        
        BorrowRequest borrowRequest = new BorrowRequest();
        borrowRequest.setUserId(userId);
        borrowRequest.setBookId(reservation.getBookId());
        borrowRequest.setNotes("Picked up from reservation: " + reservationId);
        
        try {
            borrowService.borrowBook(borrowRequest, authorization);
            reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
            reservationRepository.save(reservation);
            log.info("Book picked up and borrowed successfully: {}", reservationId);
        } catch (Exception e) {
            log.error("Failed to borrow after pickup: {}", e.getMessage());
            throw new RuntimeException("Failed to process borrowing: " + e.getMessage());
        }
        
        return ReservationResponse.fromEntity(reservation);
    }
    
    public List<ReservationResponse> getUserReservations(String userId) {
        return reservationRepository.findByUserIdOrderByReservationDateDesc(userId).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<ReservationResponse> getAllActiveReservations() {
        List<Reservation> pendingReservations = reservationRepository.findByStatusOrderByReservationDateAsc(Reservation.ReservationStatus.PENDING);
        List<Reservation> confirmedReservations = reservationRepository.findByStatusOrderByReservationDateAsc(Reservation.ReservationStatus.CONFIRMED);
        
        List<Reservation> allActive = new ArrayList<>();
        allActive.addAll(pendingReservations);
        allActive.addAll(confirmedReservations);
        
        return allActive.stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<ReservationResponse> getReservationsByBook(Long bookId) {
        return reservationRepository.findByBookIdAndStatusOrderByReservationDateAsc(
                bookId, Reservation.ReservationStatus.CONFIRMED).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void autoExpireReservations() {
        log.info("Checking for expired reservations...");
        
        List<Reservation> expiredReservations = reservationRepository.findByStatusAndExpiryDateBefore(
            Reservation.ReservationStatus.CONFIRMED, LocalDateTime.now());
        
        for (Reservation reservation : expiredReservations) {
            log.info("Expiring reservation: {}", reservation.getId());
            reservation.setStatus(Reservation.ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
        }
    }
}
