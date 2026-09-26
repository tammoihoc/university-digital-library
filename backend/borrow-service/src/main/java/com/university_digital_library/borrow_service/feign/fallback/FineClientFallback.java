package com.university_digital_library.borrow_service.feign.fallback;

import com.university_digital_library.borrow_service.dto.CreateFineRequest;
import com.university_digital_library.borrow_service.feign.FineClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FineClientFallback implements FineClient {

    @Override
    public String createFine(CreateFineRequest request, String authorization) {
        log.error("⚠️ Fine Service unavailable — không thể tạo khoản phạt cho user {} (borrowId {}, amount {}). " +
                        "Cần xử lý thủ công!",
                request.getUserId(), request.getBorrowId(), request.getAmount());
        // Không throw exception: việc trả sách vẫn nên thành công dù fine_service
        // tạm thời down, tránh chặn nghiệp vụ trả sách chỉ vì service phụ lỗi.
        return "FAILED_FINE_SERVICE_UNAVAILABLE";
    }
}
