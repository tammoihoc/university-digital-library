package com.university_digital_library.book_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    private String isbn;
    
    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookAccessType accessType;

    // HÌNH ẢNH SÁCH
    private String mainCoverImageUrl;
    
    @ElementCollection
    @CollectionTable(name = "book_images", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> additionalImageUrls = new ArrayList<>();
    
    private String thumbnailImageUrl;

    // FILE PDF
    private String pdfFileUrl;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer totalPhysicalCopies = 0;

    @Column(nullable = false)
    @Builder.Default
    private Double price = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Integer availablePhysicalCopies = 0;

    // Thông tin xuất bản
    private String publisher;
    private Integer publicationYear;
    private Integer pages;
    private String language;
    
    // Phân loại
    private String department;
    private String courseCode;
    
    @ElementCollection
    @CollectionTable(name = "book_categories", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "category")
    @Builder.Default
    private List<String> categories = new ArrayList<>();

    // Metadata
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Thông tin thống kê
    @Builder.Default
    private Integer viewCount = 0;
    @Builder.Default
    private Integer borrowCount = 0;
    @Builder.Default
    private Double averageRating = 0.0;
    @Builder.Default
    private Integer reviewCount = 0;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (totalPhysicalCopies == null) {
            totalPhysicalCopies = 0;
        }
        if (availablePhysicalCopies == null) {
            availablePhysicalCopies = totalPhysicalCopies;
        }
        if (viewCount == null) {
            viewCount = 0;
        }
        if (borrowCount == null) {
            borrowCount = 0;
        }
        if (averageRating == null) {
            averageRating = 0.0;
        }
        if (reviewCount == null) {
            reviewCount = 0;
        }
        if (categories == null) {
            categories = new ArrayList<>();
        }
        if (additionalImageUrls == null) {
            additionalImageUrls = new ArrayList<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public void incrementViewCount() {
        if (viewCount == null) viewCount = 0;
        viewCount++;
    }
    
    public void incrementBorrowCount() {
        if (borrowCount == null) borrowCount = 0;
        borrowCount++;
    }
    
    public void updateRating(Double newRating) {
        if (averageRating == null) averageRating = 0.0;
        if (reviewCount == null) reviewCount = 0;
        
        double totalRating = averageRating * reviewCount + newRating;
        reviewCount++;
        averageRating = totalRating / reviewCount;
    }
    
    // Getter/Setter
    public String getCoverImageUrl() {
        return mainCoverImageUrl;
    }
    
    public void setCoverImageUrl(String coverImageUrl) {
        this.mainCoverImageUrl = coverImageUrl;
    }
    
    // Thêm getter/setter cho các field
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BookType getType() { return type; }
    public void setType(BookType type) { this.type = type; }
    
    public BookAccessType getAccessType() { return accessType; }
    public void setAccessType(BookAccessType accessType) { this.accessType = accessType; }
    
    public String getMainCoverImageUrl() { return mainCoverImageUrl; }
    public void setMainCoverImageUrl(String mainCoverImageUrl) { this.mainCoverImageUrl = mainCoverImageUrl; }
    
    public String getPdfFileUrl() { return pdfFileUrl; }
    public void setPdfFileUrl(String pdfFileUrl) { this.pdfFileUrl = pdfFileUrl; }
    
    public Integer getTotalPhysicalCopies() { return totalPhysicalCopies; }
    public void setTotalPhysicalCopies(Integer totalPhysicalCopies) { this.totalPhysicalCopies = totalPhysicalCopies; }
    
    public Integer getAvailablePhysicalCopies() { return availablePhysicalCopies; }
    public void setAvailablePhysicalCopies(Integer availablePhysicalCopies) { this.availablePhysicalCopies = availablePhysicalCopies; }
    
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    
    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }
    
    public Integer getPages() { return pages; }
    public void setPages(Integer pages) { this.pages = pages; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    
    public Integer getBorrowCount() { return borrowCount; }
    public void setBorrowCount(Integer borrowCount) { this.borrowCount = borrowCount; }
    
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    
    public List<String> getAdditionalImageUrls() { return additionalImageUrls; }
    public void setAdditionalImageUrls(List<String> additionalImageUrls) { this.additionalImageUrls = additionalImageUrls; }
    
    public String getThumbnailImageUrl() { return thumbnailImageUrl; }
    public void setThumbnailImageUrl(String thumbnailImageUrl) { this.thumbnailImageUrl = thumbnailImageUrl; }
        @Column(nullable = false)
    @Builder.Default
    private Long libraryBranchId = 1L;  // Mặc định thư viện chính (ID=1)
    
    private String locationPrefix;  // Ví dụ: "321.1" - prefix cho vị trí
    
    // ========== GETTER/SETTER MỚI ==========
    public Long getLibraryBranchId() { return libraryBranchId; }
    public void setLibraryBranchId(Long libraryBranchId) { this.libraryBranchId = libraryBranchId; }
    
    public String getLocationPrefix() { return locationPrefix; }
    public void setLocationPrefix(String locationPrefix) { this.locationPrefix = locationPrefix; }
}
