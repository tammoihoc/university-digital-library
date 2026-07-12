// fine-service/src/main/java/.../service/impl/FineServiceImpl.java
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
                penaltyType = Fine.PenaltyType.MONEY;
            }
        }
        
        Fine fine = new Fine(
            request.getUserId(),
            request.getBorrowId(),
            request.getAmount(),
            request.getReason()
        );
        fine.setPenaltyType(penaltyType);
        fine.setCreatedBy("system");
        
        Fine saved = fineRepository.save(fine);
        log.info("Fine created with id: {}", saved.getId());
        
        return FineDTO.fromEntity(saved);
    }
    
    @Override
    @Transactional
    public FineDTO markAsPaid(Long fineId) {
        log.info("Marking fine as paid: {}", fineId);
        
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new RuntimeException("Fine not found"));
        
        if (fine.getIsPaid()) {
            throw new RuntimeException("Fine already paid");
        }
        
        fine.setIsPaid(true);
        fine.setPaidAt(LocalDateTime.now());
        
        Fine saved = fineRepository.save(fine);
        log.info("Fine marked as paid: {}", fineId);
        
        return FineDTO.fromEntity(saved);
    }
    
    @Override
    public List<FineDTO> getAllFines() {
        log.info("Getting all fines");
        return fineRepository.findAll().stream()
                .map(FineDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<FineDTO> getUserFines(String userId) {
        log.info("Getting fines for user: {}", userId);
        return fineRepository.findByUserId(userId).stream()
                .map(FineDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<FineDTO> getUnpaidFines() {
        log.info("Getting unpaid fines");
        return fineRepository.findByIsPaidFalse().stream()
                .map(FineDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> getFineStats() {
        log.info("Getting fine statistics");
        
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
        
        // Lấy giá sách từ book-service (tạm thời dùng giá mặc định nếu lỗi)
        Double bookPrice = 120000.0;
        String bookTitle = "Unknown";
        
        try {
            // Gọi Book Service để lấy giá
            Map<String, Object> bookInfo = bookClient.getBookPrice(request.getBookId(), "Bearer token");
            if (bookInfo != null && bookInfo.get("price") != null) {
                bookPrice = (Double) bookInfo.get("price");
                bookTitle = (String) bookInfo.get("title");
            }
        } catch (Exception e) {
            log.warn("Cannot get book price from Book Service, using default: {}", e.getMessage());
        }
        
        Double fineAmount = bookPrice + LOST_PENALTY_EXTRA;
        
        Fine fine = new Fine();
        fine.setUserId(request.getUserId());
        fine.setBorrowId(request.getBorrowId());
        fine.setAmount(fineAmount);
        fine.setPenaltyType(Fine.PenaltyType.MONEY);
        fine.setReason(String.format("Mất sách: %s - Phạt = giá gốc %.0fđ + 50.000đ = %.0fđ", 
                                     bookTitle, bookPrice, fineAmount));
        fine.setDamageType(Fine.DamageType.LOST);
        fine.setBookPrice(bookPrice);
        fine.setDamageDescription(request.getDescription());
        fine.setCreatedBy("SYSTEM");
        fine.setIsPaid(false);
        
        Fine saved = fineRepository.save(fine);
        log.info("Loss fine created: {} VND for book: {}", fineAmount, bookTitle);
        
        return FineDTO.fromEntity(saved);
    }
    
    @Override
    @Transactional
    public FineDTO reportDamage(ReportDamageRequest request) {
        log.info("Processing damage report for borrowId: {}", request.getBorrowId());
        
        // Lấy giá sách từ book-service (tạm thời dùng giá mặc định nếu lỗi)
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
        
        Fine fine = new Fine();
        fine.setUserId(request.getUserId());
        fine.setBorrowId(request.getBorrowId());
        fine.setAmount(fineAmount);
        fine.setPenaltyType(Fine.PenaltyType.MONEY);
        fine.setReason(String.format("Hư hỏng sách: %s - Phạt: %.0fđ", bookTitle, fineAmount));
        fine.setDamageType(damageType);
        fine.setBookPrice(bookPrice);
        fine.setDamageDescription(request.getDescription());
        fine.setCreatedBy("SYSTEM");
        fine.setIsPaid(false);
        
        Fine saved = fineRepository.save(fine);
        log.info("Damage fine created: {} VND for book: {}", fineAmount, bookTitle);
        
        return FineDTO.fromEntity(saved);
    }
}
