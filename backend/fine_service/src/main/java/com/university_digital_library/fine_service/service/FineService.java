// fine-service/src/main/java/com/university_digital_library/fine_service/service/FineService.java
package com.university_digital_library.fine_service.service;

import com.university_digital_library.fine_service.dto.CreateFineRequest;
import com.university_digital_library.fine_service.dto.FineDTO;
import com.university_digital_library.fine_service.dto.ReportDamageRequest;
import java.util.List;
import java.util.Map;

public interface FineService {

    FineDTO createFine(CreateFineRequest request);

    FineDTO payFine(Long fineId);

    List<FineDTO> getAllFines();

    List<FineDTO> getUserFines(String userId);

    List<FineDTO> getUnpaidFines(String userId);

    List<FineDTO> getOverdueFines();

    Map<String, Object> getFineStats();

    Double getTotalUnpaidAmount();  // ✅ SỬA TÊN

    FineDTO reportLoss(ReportDamageRequest request);  // ✅ SỬA TÊN

    FineDTO reportDamage(ReportDamageRequest request); // ✅ ĐÃ ĐÚNG
}
