// book-service/src/main/java/com/university_digital_library/book_service/model/Book.java
package com.university_digital_library.book_service.model;

import com.university_digital_library.common_library.security.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books", indexes = {
        @Index(name = "idx_books_library_branch", columnList = "libraryBranchId"),
        @Index(name = "idx_books_department", columnList = "department"),
        @Index(name = "idx_books_is_active", columnList = "isActive")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    // ISBN và description KHÔNG mã hóa: đây không phải dữ liệu nhạy cảm (PII),
    // và việc mã hóa AES/GCM (random IV mỗi lần) làm cho so sánh WHERE isbn = ?
    // và LIKE %keyword% trên cột mã hóa không bao giờ khớp được — khiến check
    // trùng ISBN và full-text search bị vô hiệu hoàn toàn trước đây.
    @Column(unique = true)
    private String isbn;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookAccessType accessType;

    private String coverImageUrl;
    private String pdfFileUrl;

    @Builder.Default
    private Integer totalPhysicalCopies = 0;

    @Builder.Default
    private Integer availablePhysicalCopies = 0;

    @Builder.Default
    private Double price = 0.0;

    private String publisher;
    private Integer publicationYear;
    private Integer pages;
    private String language;

    @Convert(converter = EncryptedStringConverter.class)
    private String department;

    @Convert(converter = EncryptedStringConverter.class)
    private String courseCode;

    @ElementCollection
    @CollectionTable(name = "book_categories", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "category")
    @Builder.Default
    private List<String> categories = new ArrayList<>();

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Integer viewCount = 0;

    @Builder.Default
    private Integer borrowCount = 0;

    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    private Integer reviewCount = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long libraryBranchId;
    private String locationPrefix;

    // Khôi phục optimistic locking: bản trước bị xóa @Version vì nó gây
    // OptimisticLockException với cách cập nhật đọc-rồi-ghi cũ, nhưng gốc rễ
    // vấn đề là race condition khi 2 request cùng sửa availablePhysicalCopies
    // (2 người mượn cùng lúc quyển cuối). Đã bổ sung updateAvailableCopiesAtomic
    // (UPDATE ... WHERE) ở BookRepository để tránh race condition ngay tại DB,
    // @Version vẫn giữ lại như lớp bảo vệ thứ 2 cho các đường cập nhật khác (updateBook).
    @Version
    private Long version;

    public enum BookType {
        TEXTBOOK, REFERENCE, THESIS, RESEARCH_PAPER, MAGAZINE, FICTION, NON_FICTION
    }

    public enum BookAccessType {
        FULL_DIGITAL, PREVIEW_ONLY, PHYSICAL_ONLY, HYBRID
    }
}
