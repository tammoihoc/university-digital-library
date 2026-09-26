package com.university_digital_library.borrow_service.service;

import com.university_digital_library.borrow_service.dto.BorrowRequest;
import com.university_digital_library.borrow_service.dto.BorrowResponse;
import com.university_digital_library.borrow_service.dto.ReservationRequest;
import com.university_digital_library.borrow_service.dto.ReservationResponse;

import java.util.List;

public interface BorrowService {
    // Borrow
    BorrowResponse borrowBook(BorrowRequest request, String authorization);
    BorrowResponse returnBook(Long borrowId);
    List<BorrowResponse> getUserBorrows(String userId);
    List<BorrowResponse> getCurrentBorrows(String userId);
    List<BorrowResponse> getBookBorrows(Long bookId);
    List<BorrowResponse> getActiveBorrows();
    List<BorrowResponse> getOverdueBorrows();
    List<BorrowResponse> getAllActiveBorrows();
    List<BorrowResponse> getAllBorrows();
    long getActiveBorrowsCount();

    // Reservation
    ReservationResponse createReservation(ReservationRequest request, String userId, String authorization);
    ReservationResponse cancelReservation(Long reservationId, String userId, String reason, String authorization);
    ReservationResponse confirmReservation(Long reservationId, String librarianId, String authorization);
    List<ReservationResponse> getUserReservations(String userId);
    List<ReservationResponse> getAllActiveReservations();
    List<ReservationResponse> getAllReservations();
    List<ReservationResponse> getReservationsByBook(Long bookId);
    ReservationResponse approveReservation(Long reservationId, String librarianId, String authorization);
ReservationResponse rejectReservation(Long reservationId, String reason, String librarianId, String authorization);
    void autoExpireReservations();
}
