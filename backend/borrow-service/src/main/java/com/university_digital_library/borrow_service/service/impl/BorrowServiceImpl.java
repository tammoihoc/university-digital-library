package com.university_digital_library.borrow_service.service.impl;

import com.university_digital_library.borrow_service.dto.BorrowRequest;
import com.university_digital_library.borrow_service.dto.BorrowResponse;
import com.university_digital_library.borrow_service.dto.CreateFineRequest;
import com.university_digital_library.borrow_service.dto.EntryResponseDTO;
import com.university_digital_library.borrow_service.dto.ReservationRequest;
import com.university_digital_library.borrow_service.dto.ReservationResponse;
import com.university_digital_library.borrow_service.feign.BookClient;
import com.university_digital_library.borrow_service.feign.EntryExitClient;
import com.university_digital_library.borrow_service.feign.FineClient;
import com.university_digital_library.borrow_service.feign.UserClient;
import com.university_digital_library.borrow_service.model.BorrowRecord;
import com.university_digital_library.borrow_service.model.Reservation;
import com.university_digital_library.borrow_service.repository.BorrowRepository;
import com.university_digital_library.borrow_service.repository.ReservationRepository;
import com.university_digital_library.borrow_service.service.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRepository borrowRepository;
    private final ReservationRepository reservationRepository;
    private final BookClient bookClient;
    private final UserClient userClient;
    private final EntryExitClient entryExitClient;
    private final FineClient fineClient;

    private static final int RESERVATION_HOLD_DAYS = 7;
    private static final double FINE_PER_DAY = 5000.0;

    // ----------------------------------------------------------------------
    // BORROW
    // ----------------------------------------------------------------------

    @Override
    public List<BorrowResponse> getAllBorrows() {
        return borrowRepository.findAll().stream()
                .map(BorrowResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BorrowResponse borrowBook(BorrowRequest request, String authorization) {
        String userId = request.getUserId();
        Long bookId = request.getBookId();

        log.info("📚 User {} borrowing book {}", userId, bookId);

        // 1. Kiểm tra user đã mượn sách này chưa trả chưa
        List<BorrowRecord> existingBorrows = borrowRepository.findByUserIdAndBookIdAndReturnedAtIsNull(userId, bookId);
        if (!existingBorrows.isEmpty()) {
            throw new RuntimeException("Bạn đã mượn sách này và chưa trả, không thể mượn thêm.");
        }

        // 2. Kiểm tra user có đặt trước sách này không (PENDING hoặc CONFIRMED)
        boolean hasReservation = reservationRepository.existsByUserIdAndBookIdAndStatusIn(
                userId, bookId, Arrays.asList(
                        Reservation.ReservationStatus.PENDING,
                        Reservation.ReservationStatus.CONFIRMED
                )
        );
        if (hasReservation) {
            throw new RuntimeException("Bạn đã đặt trước sách này, vui lòng đến nhận hoặc hủy đặt trước trước khi mượn.");
        }

        // 3. Kiểm tra check-in và branch từ entry-exit-service
        Map<String, Object> userStatus = entryExitClient.getUserStatus(userId, authorization);
        log.info("User status from entry-exit: {}", userStatus);
        if (userStatus == null || !Boolean.TRUE.equals(userStatus.get("checkedIn"))) {
            throw new RuntimeException("Người dùng chưa check-in vào thư viện trong ngày.");
        }
        String checkedInBranch = (String) userStatus.get("branch");
        if (checkedInBranch == null || checkedInBranch.isBlank()) {
            throw new RuntimeException("Không xác định được cơ sở đang check-in.");
        }

        // 4. Lấy thông tin sách (bao gồm libraryBranchId)
        Map<String, Object> bookDetails = bookClient.getBookDetails(bookId, authorization);
        Long libraryBranchId = ((Number) bookDetails.get("libraryBranchId")).longValue();

        // Map branch string -> id
        Long userBranchId = mapBranchToId(checkedInBranch);
        if (!libraryBranchId.equals(userBranchId)) {
            throw new RuntimeException("Sách này chỉ có ở cơ sở khác, không thể mượn tại cơ sở hiện tại.");
        }

        // 5. Kiểm tra số lượng sách
        Map<String, Object> availability = bookClient.getBookAvailability(bookId, authorization);
        Integer availableCopies = (Integer) availability.get("availablePhysicalCopies");
        Boolean canBeBorrowed = (Boolean) availability.get("canBeBorrowed");

        if (!Boolean.TRUE.equals(canBeBorrowed) || availableCopies == null || availableCopies <= 0) {
            throw new RuntimeException("Sách hiện không có sẵn để mượn.");
        }

        // 6. Kiểm tra hạn mức mượn và trạng thái khóa
        Map<String, Object> userInfo = userClient.getUserBorrowInfo(userId, authorization);
        Integer currentBorrowed = (Integer) userInfo.get("currentBorrowed");
        Integer maxBorrowLimit = (Integer) userInfo.get("maxBorrowLimit");
        Boolean isLocked = (Boolean) userInfo.get("isLocked");
        if (isLocked != null && isLocked) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa quyền mượn sách.");
        }
        if (currentBorrowed != null && maxBorrowLimit != null && currentBorrowed >= maxBorrowLimit) {
            throw new RuntimeException("Người dùng đã đạt giới hạn mượn tối đa: " + maxBorrowLimit);
        }

        // 7. Giảm số lượng sách TRƯỚC khi tạo BorrowRecord, dùng API nguyên tử ở
        // book-service (UPDATE ... WHERE availableCopies > 0) thay vì đọc rồi tự trừ —
        // cách cũ có race condition: 2 người mượn cùng lúc đều đọc thấy availableCopies=1,
        // cả 2 đều tạo được BorrowRecord dù thực tế chỉ còn 1 quyển.
        Map<String, Object> decrementResult = bookClient.decrementAvailableCopies(bookId, authorization);
        if (!Boolean.TRUE.equals(decrementResult.get("success"))) {
            throw new RuntimeException("Sách vừa hết trong lúc xử lý, vui lòng thử lại.");
        }

        // 8. Tạo BorrowRecord
        BorrowRecord record = BorrowRecord.builder()
                .userId(userId)
                .bookId(bookId)
                .borrowedAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(14))
                .status(BorrowRecord.BorrowStatus.ACTIVE)
                .notes(request.getNotes())
                .borrowedLocation(checkedInBranch)
                .build();

        BorrowRecord saved = borrowRepository.save(record);

        // 9. Cập nhật số sách đang mượn của user
        if (currentBorrowed != null) {
            userClient.updateBorrowCount(userId, currentBorrowed + 1, authorization);
        }

        log.info("✅ Borrowed successfully: {}", saved.getId());
        return BorrowResponse.fromEntity(saved);
    }

    // Helper: Map branch string -> id
    private Long mapBranchToId(String branch) {
        if (branch == null) return 1L;
        return switch (branch.toUpperCase()) {
            case "B" -> 1L;
            case "E" -> 2L;
            default -> 1L;
        };
    }

    @Override
    @Transactional
    public BorrowResponse returnBook(Long borrowId) {
        BorrowRecord record = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new RuntimeException("Borrow record not found: " + borrowId));

        if (record.getReturnedAt() != null) {
            throw new RuntimeException("Sách đã được trả trước đó.");
        }

        double fine = 0.0;
        if (LocalDateTime.now().isAfter(record.getDueDate())) {
            // Trước đây dùng Duration.toDays() sẽ LÀM TRÒN XUỐNG: trễ 12 tiếng (0.5 ngày)
            // cho ra daysOverdue = 0 -> fine = 0đ dù sách rõ ràng đã trễ hạn.
            // Dùng ceiling: hễ đã trễ (dù chỉ 1 phút) thì tính tối thiểu 1 ngày.
            long minutesOverdue = java.time.Duration.between(record.getDueDate(), LocalDateTime.now()).toMinutes();
            long daysOverdue = (long) Math.ceil(minutesOverdue / (24.0 * 60));
            if (daysOverdue < 1) daysOverdue = 1;
            fine = daysOverdue * FINE_PER_DAY;
            record.setFineAmount(fine);

            // Tạo khoản phạt bên fine_service để user có thể tra cứu/thanh toán qua API /fines
            try {
                CreateFineRequest fineRequest = CreateFineRequest.builder()
                        .userId(record.getUserId())
                        .borrowId(record.getId())
                        .amount(fine)
                        .penaltyType("MONEY")
                        .reason(String.format("Trả sách trễ %d ngày (bookId: %d)", daysOverdue, record.getBookId()))
                        .build();
                fineClient.createFine(fineRequest, null);
                log.info("💰 Created fine {} VND for user {} due to late return", fine, record.getUserId());
            } catch (Exception e) {
                log.error("Error creating fine for late return: {}", e.getMessage());
            }
        }

        record.setReturnedAt(LocalDateTime.now());
        record.setStatus(BorrowRecord.BorrowStatus.RETURNED);
        BorrowRecord saved = borrowRepository.save(record);

        // Cập nhật số lượng sách — dùng API nguyên tử, không cần đọc trước rồi tính +1
        try {
            bookClient.incrementAvailableCopies(record.getBookId(), null);
            log.info("✅ Increased available copies for book {}", record.getBookId());
        } catch (Exception e) {
            log.error("Error updating book availability: {}", e.getMessage());
        }

        // Cập nhật số sách đang mượn của user
        try {
            Map<String, Object> userInfo = userClient.getUserBorrowInfo(record.getUserId(), null);
            Integer current = (Integer) userInfo.get("currentBorrowed");
            if (current != null && current > 0) {
                userClient.updateBorrowCount(record.getUserId(), current - 1, null);
                log.info("✅ Decreased borrowed count for user {} to {}", record.getUserId(), current - 1);
            }
        } catch (Exception e) {
            log.error("Error updating user borrow count: {}", e.getMessage());
        }

        // Nếu borrow được tạo từ reservation, cập nhật reservation thành COMPLETED
        if (record.getReservationId() != null) {
            try {
                Long reservationId = Long.parseLong(record.getReservationId());
                Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
                if (reservation != null && reservation.getStatus() == Reservation.ReservationStatus.CONFIRMED) {
                    reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
                    reservation.setConfirmedAt(LocalDateTime.now());
                    reservationRepository.save(reservation);
                    log.info("✅ Reservation {} marked as COMPLETED after return", reservationId);
                }
            } catch (Exception e) {
                log.error("Error updating reservation status on return: {}", e.getMessage());
            }
        }

        return BorrowResponse.fromEntity(saved);
    }

    // ----------------------------------------------------------------------
    // RESERVATION
    // ----------------------------------------------------------------------

    @Override
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, String userId, String authorization) {
        Long bookId = request.getBookId();
        log.info("🔖 Creating reservation for user {} - book {}", userId, bookId);

        // 1. Check existing borrow
        List<BorrowRecord> existingBorrows = borrowRepository.findByUserIdAndBookIdAndReturnedAtIsNull(userId, bookId);
        if (!existingBorrows.isEmpty()) {
            throw new RuntimeException("Bạn đã mượn sách này và chưa trả, không thể đặt trước.");
        }

        // 2. Check existing active reservation (PENDING or CONFIRMED)
        boolean hasActiveReservation = reservationRepository.existsByUserIdAndBookIdAndStatusIn(
                userId, bookId, Arrays.asList(
                        Reservation.ReservationStatus.PENDING,
                        Reservation.ReservationStatus.CONFIRMED
                )
        );
        if (hasActiveReservation) {
            throw new RuntimeException("Bạn đã có đặt trước đang hoạt động cho sách này.");
        }

        // 3. Check availability
        Map<String, Object> availability = bookClient.getBookAvailability(bookId, authorization);
        Integer availableCopies = (Integer) availability.get("availablePhysicalCopies");
        Boolean canBeBorrowed = (Boolean) availability.get("canBeBorrowed");

        if (!Boolean.TRUE.equals(canBeBorrowed) || availableCopies == null || availableCopies <= 0) {
            throw new RuntimeException("Sách hiện không có sẵn để đặt trước.");
        }

        // 4. Xác định pickupDate
        LocalDate pickupDate = request.getPickupDate();
        if (pickupDate == null) {
            pickupDate = LocalDate.now().plusDays(1);
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pickupDateTime = pickupDate.atStartOfDay();
        LocalDateTime expiryDateTime = pickupDateTime.plusDays(1);

        // Lỗi cũ: pickupDateTime = 00:00 của pickupDate. Nếu người dùng chọn NGAY HÔM NAY
        // thì 00:00 hôm nay luôn nằm TRƯỚC thời điểm hiện tại (now) -> validate luôn báo lỗi
        // dù đặt trước cho hôm nay là hợp lệ. So sánh theo NGÀY (LocalDate) thay vì so
        // theo mốc thời gian chính xác để cho phép đặt trước ngay trong ngày hôm nay.
        LocalDate today = LocalDate.now();
        if (pickupDate.isBefore(today) || pickupDate.isAfter(today.plusDays(7))) {
            throw new RuntimeException("Ngày nhận sách phải từ hôm nay đến 7 ngày tới.");
        }

        // 5. Lấy thông tin sách
        Map<String, Object> bookDetails = bookClient.getBookDetails(bookId, authorization);

        // ✅ KHÔNG GIẢM SỐ LƯỢNG SÁCH KHI TẠO RESERVATION
        Reservation reservation = Reservation.builder()
                .userId(userId)
                .bookId(bookId)
                .reservationDate(now)
                .pickupDate(pickupDateTime)
                .expiryDate(expiryDateTime)
                .status(Reservation.ReservationStatus.PENDING)
                .bookTitle((String) bookDetails.get("title"))
                .bookAuthor((String) bookDetails.get("author"))
                .notes(request.getNotes())
                .build();

        Reservation saved = reservationRepository.save(reservation);

        log.info("✅ Reservation created: {}", saved.getId());
        return ReservationResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public ReservationResponse approveReservation(Long reservationId, String librarianId, String authorization) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể xác nhận các đặt trước ở trạng thái PENDING");
        }

        // Kiểm tra sách còn không
        Map<String, Object> availability = bookClient.getBookAvailability(reservation.getBookId(), authorization);
        Integer availableCopies = (Integer) availability.get("availablePhysicalCopies");
        if (availableCopies == null || availableCopies <= 0) {
            throw new RuntimeException("Sách hiện không có sẵn để xác nhận đặt trước");
        }

        // ✅ GIẢM SỐ LƯỢNG SÁCH TẠI ĐÂY (giữ sách cho người dùng) — dùng API nguyên tử
        Map<String, Object> decrementResult = bookClient.decrementAvailableCopies(reservation.getBookId(), authorization);
        if (!Boolean.TRUE.equals(decrementResult.get("success"))) {
            throw new RuntimeException("Sách vừa hết trong lúc xử lý, không thể xác nhận đặt trước.");
        }

        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        reservation.setConfirmedAt(LocalDateTime.now());
        reservation.setConfirmedBy(librarianId);
        // Đặt thời hạn 7 ngày kể từ khi xác nhận
        LocalDateTime now = LocalDateTime.now();
        reservation.setExpiryDate(now.plusDays(7));
        Reservation saved = reservationRepository.save(reservation);

        log.info("✅ Reservation {} approved by librarian {}, held for 7 days", reservationId, librarianId);
        return ReservationResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public ReservationResponse rejectReservation(Long reservationId, String reason, String librarianId, String authorization) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể từ chối các đặt trước ở trạng thái PENDING");
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        reservation.setCancelReason(reason != null ? reason : "Bị từ chối bởi thủ thư");
        Reservation saved = reservationRepository.save(reservation);

        log.info("❌ Reservation {} rejected by librarian {}", reservationId, librarianId);
        return ReservationResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public ReservationResponse confirmReservation(Long reservationId, String librarianId, String authorization) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));

        if (reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw new RuntimeException("Đặt trước không ở trạng thái CONFIRMED.");
        }

        // ✅ Kiểm tra đã có BorrowRecord cho reservation này chưa
        List<BorrowRecord> existingRecords = borrowRepository.findByReservationId(reservationId.toString());
        if (!existingRecords.isEmpty()) {
            throw new RuntimeException("Đã có BorrowRecord cho đặt trước này. Không thể xác nhận lại.");
        }

        String userId = reservation.getUserId();
        Long bookId = reservation.getBookId();

        // Kiểm tra user đã mượn sách này chưa trả chưa
        List<BorrowRecord> existingBorrows = borrowRepository.findByUserIdAndBookIdAndReturnedAtIsNull(userId, bookId);
        if (!existingBorrows.isEmpty()) {
            throw new RuntimeException("User already borrowed this book and not returned.");
        }

        // Kiểm tra hạn mức và khóa
        Map<String, Object> userInfo = userClient.getUserBorrowInfo(userId, authorization);
        Integer currentBorrowed = (Integer) userInfo.get("currentBorrowed");
        Integer maxBorrowLimit = (Integer) userInfo.get("maxBorrowLimit");
        Boolean isLocked = (Boolean) userInfo.get("isLocked");
        if (isLocked != null && isLocked) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa quyền mượn sách.");
        }
        if (currentBorrowed != null && maxBorrowLimit != null && currentBorrowed >= maxBorrowLimit) {
            throw new RuntimeException("User has reached maximum borrow limit: " + maxBorrowLimit);
        }

        // ✅ Tạo BorrowRecord (KHÔNG GIẢM SỐ LƯỢNG SÁCH)
        BorrowRecord record = BorrowRecord.builder()
                .userId(userId)
                .bookId(bookId)
                .borrowedAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(14))
                .status(BorrowRecord.BorrowStatus.ACTIVE)
                .notes("Borrowed from reservation #" + reservationId)
                .borrowedLocation(reservation.getBookLocation())
                .reservationId(reservationId.toString())
                .build();
        borrowRepository.save(record);

        // Cập nhật số sách đang mượn của user
        if (currentBorrowed != null) {
            userClient.updateBorrowCount(userId, currentBorrowed + 1, authorization);
        }

        // Đánh dấu reservation hoàn thành
        reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
        reservation.setConfirmedAt(LocalDateTime.now());
        reservation.setConfirmedBy(librarianId);
        reservationRepository.save(reservation);

        log.info("✅ Reservation {} confirmed and borrowed by user {}", reservationId, userId);
        return ReservationResponse.fromEntity(reservation);
    }

    @Override
    @Transactional // trước đây thiếu @Transactional dù có nhiều thao tác ghi liên quan tới nhau
    public ReservationResponse cancelReservation(Long reservationId, String userId, String reason, String authorization) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));

        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn chỉ có thể hủy đặt trước của chính mình.");
        }

        if (reservation.getStatus() == Reservation.ReservationStatus.COMPLETED) {
            throw new RuntimeException("Không thể hủy đặt trước đã hoàn thành.");
        }

        // Nếu đang ở trạng thái CONFIRMED, trả lại số lượng
        if (reservation.getStatus() == Reservation.ReservationStatus.CONFIRMED) {
            try {
                bookClient.incrementAvailableCopies(reservation.getBookId(), authorization);
                log.info("🔄 Increased available copies for book {}", reservation.getBookId());
            } catch (Exception e) {
                log.error("Error returning book: {}", e.getMessage());
            }
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        reservation.setCancelReason(reason != null ? reason : "User cancelled");
        Reservation saved = reservationRepository.save(reservation);

        return ReservationResponse.fromEntity(saved);
    }

    // ----------------------------------------------------------------------
    // SCHEDULED: TỰ ĐỘNG HẾT HẠN
    // ----------------------------------------------------------------------

    @Override
    @Scheduled(fixedDelay = 3600000) // mỗi giờ
    @Transactional
    public void autoExpireReservations() {
        log.info("🕒 Checking for expired reservations...");

        List<Reservation> expired = reservationRepository.findByStatusAndPickupDateBefore(
                Reservation.ReservationStatus.CONFIRMED, LocalDateTime.now());

        for (Reservation reservation : expired) {
            log.info("⏳ Expiring reservation: {}", reservation.getId());
            reservation.setStatus(Reservation.ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);

            // Trả lại số lượng sách
            try {
                bookClient.incrementAvailableCopies(reservation.getBookId(), null);
                log.info("🔁 Increased available copies for book {}", reservation.getBookId());
            } catch (Exception e) {
                log.error("Error returning book for expired reservation: {}", e.getMessage());
            }
        }
    }

    // ========== Các phương thức get khác ==========

    @Override
    public List<BorrowResponse> getUserBorrows(String userId) {
        return borrowRepository.findByUserId(userId).stream()
                .map(BorrowResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowResponse> getCurrentBorrows(String userId) {
        return borrowRepository.findByUserIdAndReturnedAtIsNull(userId).stream()
                .map(BorrowResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowResponse> getBookBorrows(Long bookId) {
        return borrowRepository.findByBookId(bookId).stream()
                .map(BorrowResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowResponse> getActiveBorrows() {
        return getAllActiveBorrows();
    }

    @Override
    public List<BorrowResponse> getOverdueBorrows() {
        return borrowRepository.findByDueDateBeforeAndReturnedAtIsNull(LocalDateTime.now()).stream()
                .map(BorrowResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowResponse> getAllActiveBorrows() {
        List<BorrowRecord> active = borrowRepository.findByStatus(BorrowRecord.BorrowStatus.ACTIVE);
        List<BorrowRecord> overdue = borrowRepository.findByStatus(BorrowRecord.BorrowStatus.OVERDUE);
        List<BorrowRecord> all = new ArrayList<>();
        all.addAll(active);
        all.addAll(overdue);
        return all.stream()
                .map(BorrowResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long getActiveBorrowsCount() {
        long active = borrowRepository.countByStatus(BorrowRecord.BorrowStatus.ACTIVE);
        long overdue = borrowRepository.countByStatus(BorrowRecord.BorrowStatus.OVERDUE);
        return active + overdue;
    }

    @Override
    public List<ReservationResponse> getUserReservations(String userId) {
        return reservationRepository.findByUserIdOrderByReservationDateDesc(userId).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationResponse> getAllActiveReservations() {
        return reservationRepository.findByStatus(Reservation.ReservationStatus.CONFIRMED).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationResponse> getReservationsByBook(Long bookId) {
        return reservationRepository.findByBookId(bookId).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
