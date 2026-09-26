// fine-service/src/main/java/com/university_digital_library/fine_service/service/PenaltyService.java
package com.university_digital_library.fine_service.service;

import com.university_digital_library.fine_service.dto.PenaltyRequest;
import com.university_digital_library.fine_service.dto.PenaltyResponse;
import com.university_digital_library.fine_service.model.Penalty;
import com.university_digital_library.fine_service.repository.PenaltyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PenaltyService {
    
    private final PenaltyRepository penaltyRepository;
    
    @Transactional
    public PenaltyResponse createPenalty(PenaltyRequest request, String createdBy) {
        log.info("Creating penalty for user: {}, type: {}", request.getUserId(), request.getPenaltyType());
        
        Penalty.PenaltyType penaltyType = Penalty.PenaltyType.valueOf(request.getPenaltyType().toUpperCase());
        Penalty.PenaltyLevel level = request.getLevel() != null ? 
            Penalty.PenaltyLevel.valueOf(request.getLevel().toUpperCase()) : 
            Penalty.PenaltyLevel.WARNING;
        
        Penalty penalty = Penalty.builder()
            .userId(request.getUserId())
            .penaltyType(penaltyType)
            .reason(request.getReason())
            .level(level)
            .newBorrowLimit(request.getNewBorrowLimit())
            .lockedUntil(request.getLockedUntil())
            .isActive(true)
            .createdBy(createdBy)
            .build();
        
        Penalty saved = penaltyRepository.save(penalty);
        log.info("Penalty created with id: {}", saved.getId());
        
        return PenaltyResponse.fromEntity(saved);
    }
    
    public List<PenaltyResponse> getUserPenalties(String userId) {
        return penaltyRepository.findByUserId(userId).stream()
            .map(PenaltyResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    public List<PenaltyResponse> getActivePenalties() {
        return penaltyRepository.findByIsActiveTrue().stream()
            .map(PenaltyResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    public List<PenaltyResponse> getAllPenalties() {
        return penaltyRepository.findAll().stream()
            .map(PenaltyResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public PenaltyResponse resolvePenalty(Long penaltyId) {
        Penalty penalty = penaltyRepository.findById(penaltyId)
            .orElseThrow(() -> new RuntimeException("Penalty not found: " + penaltyId));
        
        penalty.setIsActive(false);
        Penalty saved = penaltyRepository.save(penalty);
        log.info("Penalty resolved: {}", penaltyId);
        
        return PenaltyResponse.fromEntity(saved);
    }
}
