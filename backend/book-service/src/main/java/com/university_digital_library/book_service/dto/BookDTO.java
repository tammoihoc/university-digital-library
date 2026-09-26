package com.university_digital_library.book_service.dto;

import com.university_digital_library.book_service.model.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private String type;
    private String accessType;
    private String coverImageUrl;
    private String pdfFileUrl;
    private Integer totalPhysicalCopies;
    private Integer availablePhysicalCopies;
    private Double price;
    private String publisher;
    private Integer publicationYear;
    private Integer pages;
    private String language;
    private String department;
    private String courseCode;
    private List<String> categories;
    private Boolean isActive;
    private Integer viewCount;
    private Integer borrowCount;
    private Double averageRating;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean canReadOnline;
    private Boolean canBorrowPhysical;
    private Boolean canAccess;
        private Long libraryBranchId;
    public static BookDTO fromEntity(Book book) {
        boolean canReadOnline = book.getPdfFileUrl() != null && 
            (book.getAccessType() == Book.BookAccessType.FULL_DIGITAL || 
             book.getAccessType() == Book.BookAccessType.HYBRID);
        
        boolean canBorrowPhysical = book.getAvailablePhysicalCopies() > 0 && 
            (book.getAccessType() == Book.BookAccessType.PHYSICAL_ONLY || 
             book.getAccessType() == Book.BookAccessType.HYBRID);
        
        return BookDTO.builder()
            .id(book.getId())
            .title(book.getTitle())
            .author(book.getAuthor())
            .isbn(book.getIsbn())
            .description(book.getDescription())
            .type(book.getType() != null ? book.getType().name() : null)
            .accessType(book.getAccessType() != null ? book.getAccessType().name() : null)
            .coverImageUrl(book.getCoverImageUrl())
            .pdfFileUrl(book.getPdfFileUrl())
            .totalPhysicalCopies(book.getTotalPhysicalCopies())
            .availablePhysicalCopies(book.getAvailablePhysicalCopies())
            .price(book.getPrice())
            .publisher(book.getPublisher())
            .publicationYear(book.getPublicationYear())
            .pages(book.getPages())
            .language(book.getLanguage())
            .department(book.getDepartment())
            .courseCode(book.getCourseCode())
            .categories(book.getCategories())
            .isActive(book.getIsActive())
            .viewCount(book.getViewCount())
            .borrowCount(book.getBorrowCount())
            .averageRating(book.getAverageRating())
            .reviewCount(book.getReviewCount())
            .createdAt(book.getCreatedAt())
            .updatedAt(book.getUpdatedAt())
            .canReadOnline(canReadOnline)
            .canBorrowPhysical(canBorrowPhysical)
            .canAccess(canReadOnline || canBorrowPhysical)
            .libraryBranchId(book.getLibraryBranchId())
            .build();
    }
}
