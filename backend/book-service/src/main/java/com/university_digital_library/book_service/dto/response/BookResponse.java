package com.university_digital_library.book_service.dto.response;

import com.university_digital_library.book_service.model.BookAccessType;
import com.university_digital_library.book_service.model.BookType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private BookType type;
    private BookAccessType accessType;
    
    // HÌNH ẢNH
    private String mainCoverImageUrl;
    private String coverImageUrl;
    
    @Builder.Default
    private List<String> additionalImageUrls = new ArrayList<>();
    
    private String thumbnailImageUrl;
    
    // FILE PDF
    private String pdfFileUrl;
    private Long pdfFileSize;
    private Integer pdfPageCount;
    
    @Builder.Default
    private Boolean hasPdfPreview = false;
    
    // THÔNG TIN VẬT LÝ
    private Integer totalPhysicalCopies;
    private Integer availablePhysicalCopies;
    
    // THÔNG TIN XUẤT BẢN
    private String publisher;
    private Integer publicationYear;
    private Integer pages;
    private String language;
    private String edition;
    
    // PHÂN LOẠI
    private String department;
    private String courseCode;
    
    @Builder.Default
    private List<String> categories = new ArrayList<>();
    
    // TRẠNG THÁI TRUY CẬP
    @Builder.Default
    private Boolean canReadOnline = false;
    
    @Builder.Default
    private Boolean canBorrowPhysical = false;
    
    @Builder.Default
    private Boolean canAccess = false;
    
    // THỐNG KÊ
    @Builder.Default
    private Integer viewCount = 0;
    
    @Builder.Default
    private Integer borrowCount = 0;
    
    @Builder.Default
    private Double averageRating = 0.0;
    
    @Builder.Default
    private Integer reviewCount = 0;
    
    // METADATA
    private LocalDateTime createdAt;
    
    // QR/BARCODE
    private String qrCodeUrl;
    private String barcode;
    
    // Getter cho backward compatibility
    public String getCoverImageUrl() {
        return mainCoverImageUrl != null ? mainCoverImageUrl : coverImageUrl;
    }
    
    // Thêm các getter boolean cho consistency
    public Boolean isCanReadOnline() { return canReadOnline; }
    public Boolean isCanBorrowPhysical() { return canBorrowPhysical; }
    public Boolean isCanAccess() { return canAccess; }
    
    // Getter cho các field khác
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public String getDescription() { return description; }
    public BookType getType() { return type; }
    public BookAccessType getAccessType() { return accessType; }
    public Integer getTotalPhysicalCopies() { return totalPhysicalCopies; }
    public Integer getAvailablePhysicalCopies() { return availablePhysicalCopies; }
    public String getPublisher() { return publisher; }
    public Integer getPublicationYear() { return publicationYear; }
    public Integer getPages() { return pages; }
    public String getLanguage() { return language; }
    public String getDepartment() { return department; }
    public String getCourseCode() { return courseCode; }
    public List<String> getCategories() { return categories; }
    public Integer getViewCount() { return viewCount; }
    public Integer getBorrowCount() { return borrowCount; }
    public Double getAverageRating() { return averageRating; }
    public Integer getReviewCount() { return reviewCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
