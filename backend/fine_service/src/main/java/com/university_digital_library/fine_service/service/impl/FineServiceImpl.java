// fine-service/src/main/java/com/university_digital_library/fine_service/service/impl/FineServiceImpl.java
package com.university_digital_library.fine_service.service.impl;

import com.university_digital_library.fine_service.dto.CreateFineRequest;
import com.university_digital_library.fine_service.dto.FineDTO;
import com.university_digital_library.fine_service.dto.ReportDamageRequest;
import com.university_digital_library.fine_service.feign.BookClient;
import com.university_digital_library.fine_service.model.Fine;
import com.university_digital_library.fine_service.repository.FineRepository;
import com.university_digital_library.fine_service.service.FineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FineServiceImpl implements FineService {

    private final FineRepository fineRepository;
    private final BookClient bookClient;

    private static final double LOST_PENALTY_EXTRA = 50000.0;

    @Override
    @Transactional
    public FineDTO createFine(CreateFineRequest request) {
        log.info("Creating fine for user: {}, amount: {}", request.getUserId(), request.getAmount());

        Fine.PenaltyType penaltyType = Fine.PenaltyType.MONEY;
        if (request.getPenaltyType() != null) {
            try {
                penaltyType = Fine.PenaltyType.valueOf(request.getPenaltyType().toUpperCase());
            } catch (IllegalArgumentException e) {
                // giữ mặc định
            }
        }

        Fine fine = Fine.builder()
                .userId(request.getUserId())
                .borrowId(request.getBorrowId())
                .amount(request.getAmount())
                .penaltyType(penaltyType)
                .reason(request.getReason())
                .isPaid(false)
                .createdBy("system")
                .build();

        Fine saved = fineRepository.save(fine);
        log.info("Fine created with id: {}", saved.getId());
        return FineDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public FineDTO payFine(Long fineId) {
        log.info("Paying fine: {}", fineId);

        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new RuntimeException("Fine not found"));

        if (fine.getIsPaid()) {
            throw new RuntimeException("Fine already paid");
        }

        fine.setIsPaid(true);
        fine.setPaidAt(LocalDateTime.now());

        Fine saved = fineRepository.save(fine);
        log.info("Fine paid: {}", fineId);
        return FineDTO.fromEntity(saved);
    }

    @Override
    public List<FineDTO> getAllFines() {
        return fineRepository.findAll().stream()
                .map(FineDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<FineDTO> getUserFines(String userId) {
        return fineRepository.findByUserId(userId).stream()
                .map(FineDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<FineDTO> getUnpaidFines(String userId) {
        return fineRepository.findByUserIdAndIsPaidFalse(userId).stream()
                .map(FineDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<FineDTO> getOverdueFines() {
        // Giả sử các khoản phạt quá hạn là các khoản chưa thanh toán quá 7 ngày
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return fineRepository.findAll().stream()
                .filter(f -> !f.getIsPaid() && f.getCreatedAt().isBefore(sevenDaysAgo))
                .map(FineDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getFineStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUnpaidCount", fineRepository.countByIsPaidFalse());
        stats.put("totalUnpaidAmount", fineRepository.sumUnpaidFines());
        stats.put("totalFines", fineRepository.count());
        return stats;
    }

    @Override
    public Double getTotalUnpaidAmount() {
        Double amount = fineRepository.sumUnpaidFines();
        return amount != null ? amount : 0.0;
    }

    @Override
    @Transactional
    public FineDTO reportLoss(ReportDamageRequest request) {
        log.info("Processing loss report for borrowId: {}", request.getBorrowId());

        Double bookPrice = 120000.0;
        String bookTitle = "Unknown";

        try {
            Map<String, Object> bookInfo = bookClient.getBookPrice(request.getBookId(), "Bearer token");
            if (bookInfo != null && bookInfo.get("price") != null) {
                bookPrice = (Double) bookInfo.get("price");
                bookTitle = (String) bookInfo.get("title");
            }
        } catch (Exception e) {
            log.warn("Cannot get book price from Book Service, using default: {}", e.getMessage());
        }

        Double fineAmount = bookPrice + LOST_PENALTY_EXTRA;

        Fine fine = Fine.builder()
                .userId(request.getUserId())
                .borrowId(request.getBorrowId())
                .amount(fineAmount)
                .penaltyType(Fine.PenaltyType.MONEY)
                .reason(String.format("Mất sách: %s - Phạt = giá gốc %.0fđ + 50.000đ = %.0fđ",
                        bookTitle, bookPrice, fineAmount))
                .damageType(Fine.DamageType.LOST)
                .bookPrice(bookPrice)
                .damageDescription(request.getDescription())
                .createdBy("SYSTEM")
                .isPaid(false)
                .build();

        Fine saved = fineRepository.save(fine);
        log.info("Loss fine created: {} VND for book: {}", fineAmount, bookTitle);
        return FineDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public FineDTO reportDamage(ReportDamageRequest request) {
        log.info("Processing damage report for borrowId: {}", request.getBorrowId());

        Double bookPrice = 120000.0;
        String bookTitle = "Unknown";

        try {
            Map<String, Object> bookInfo = bookClient.getBookPrice(request.getBookId(), "Bearer token");
            if (bookInfo != null && bookInfo.get("price") != null) {
                bookPrice = (Double) bookInfo.get("price");
                bookTitle = (String) bookInfo.get("title");
            }
        } catch (Exception e) {
            log.warn("Cannot get book price from Book Service, using default: {}", e.getMessage());
        }

        Double fineAmount;
        Fine.DamageType damageType;

        if ("DAMAGED_HEAVY".equals(request.getDamageType())) {
            fineAmount = bookPrice + LOST_PENALTY_EXTRA;
            damageType = Fine.DamageType.DAMAGED_HEAVY;
        } else {
            fineAmount = (bookPrice * 0.5) + 30000;
            damageType = Fine.DamageType.DAMAGED_LIGHT;
        }

        Fine fine = Fine.builder()
                .userId(request.getUserId())
                .borrowId(request.getBorrowId())
                .amount(fineAmount)
                .penaltyType(Fine.PenaltyType.MONEY)
                .reason(String.format("Hư hỏng sách: %s - Phạt: %.0fđ", bookTitle, fineAmount))
                .damageType(damageType)
                .bookPrice(bookPrice)
                .damageDescription(request.getDescription())
                .createdBy("SYSTEM")
                .isPaid(false)
                .build();

        Fine saved = fineRepository.save(fine);
        log.info("Damage fine created: {} VND for book: {}", fineAmount, bookTitle);
        return FineDTO.fromEntity(saved);
    }
}
