// fine-service/src/main/java/.../service/FineService.java
package com.university_digital_library.fine_service.service;

import com.university_digital_library.fine_service.dto.CreateFineRequest;
import com.university_digital_library.fine_service.dto.FineDTO;
import com.university_digital_library.fine_service.dto.ReportDamageRequest;
import java.util.List;
import java.util.Map;

public interface FineService {
    FineDTO createFine(CreateFineRequest request);
    FineDTO markAsPaid(Long fineId);
    List<FineDTO> getAllFines();
    List<FineDTO> getUserFines(String userId);
    List<FineDTO> getUnpaidFines();
    Map<String, Object> getFineStats();
    Double getTotalUnpaidAmount();
    
    // Thêm 2 method mới
    FineDTO reportLoss(ReportDamageRequest request);
    FineDTO reportDamage(ReportDamageRequest request);
}
