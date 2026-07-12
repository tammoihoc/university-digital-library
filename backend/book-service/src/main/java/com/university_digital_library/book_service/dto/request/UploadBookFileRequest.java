package com.university_digital_library.book_service.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadBookFileRequest {
    private MultipartFile coverImage;
    private MultipartFile pdfFile;
}
