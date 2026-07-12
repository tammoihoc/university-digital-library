package com.university_digital_library.book_service.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class UploadBookImagesRequest {
    private MultipartFile mainCoverImage;
    private List<MultipartFile> additionalImages;
    private MultipartFile thumbnailImage;
    private MultipartFile publisherLogo;
    
    // Validation
    public void validate() {
        if (mainCoverImage == null || mainCoverImage.isEmpty()) {
            throw new IllegalArgumentException("Main cover image is required");
        }
        if (!isValidImageFormat(mainCoverImage)) {
            throw new IllegalArgumentException("Main cover image must be JPG, PNG, or WEBP format");
        }
    }
    
    private boolean isValidImageFormat(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/webp") ||
            contentType.equals("image/jpg")
        );
    }
}
