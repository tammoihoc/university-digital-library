package com.university_digital_library.book_service.service.impl;

import com.university_digital_library.book_service.dto.request.CreateBookRequest;
import com.university_digital_library.book_service.dto.response.BookResponse;
import com.university_digital_library.book_service.model.*;
import com.university_digital_library.book_service.repository.BookRepository;
import com.university_digital_library.book_service.service.BookService;
import com.university_digital_library.book_service.util.PdfUtil;
import com.university_digital_library.book_service.repository.LibraryBranchRepository;
import com.university_digital_library.book_service.model.LibraryBranch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.university_digital_library.book_service.repository.BookLocationRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final PdfUtil pdfUtil;
    
private final BookLocationRepository bookLocationRepository;
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
private final LibraryBranchRepository libraryBranchRepository;
    // ========== CREATE BOOK ==========
    @Override
    public BookResponse createBook(CreateBookRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Book title is required");
        }
        if (request.getAuthor() == null || request.getAuthor().trim().isEmpty()) {
            throw new RuntimeException("Book author is required");
        }
        if (request.getType() == null) {
            throw new RuntimeException("Book type is required");
        }
        if (request.getAccessType() == null) {
            throw new RuntimeException("Book access type is required");
        }

        if (request.getIsbn() != null && !request.getIsbn().trim().isEmpty() 
            && bookRepository.findByIsbn(request.getIsbn()).isPresent()) {
            throw new RuntimeException("Book with ISBN " + request.getIsbn() + " already exists");
        }

Book book = Book.builder()
    .title(request.getTitle().trim())
    .author(request.getAuthor().trim())
    .isbn(request.getIsbn() != null ? request.getIsbn().trim() : null)
    .description(request.getDescription())
    .type(request.getType())
    .accessType(request.getAccessType())
    .publisher(request.getPublisher())
    .publicationYear(request.getPublicationYear())
    .pages(request.getPages())
    .totalPhysicalCopies(request.getTotalPhysicalCopies() != null ? request.getTotalPhysicalCopies() : 0)
    .availablePhysicalCopies(request.getTotalPhysicalCopies() != null ? request.getTotalPhysicalCopies() : 0)
    .department(request.getDepartment())
    .courseCode(request.getCourseCode())
    .categories(request.getCategories() != null ? request.getCategories() : new ArrayList<>())
    .price(request.getPrice() != null ? request.getPrice() : 0.0)  // ✅ THÊM DÒNG NÀY
    .build();
        Book savedBook = bookRepository.save(book);
        log.info("Created new book: {}", savedBook.getTitle());
        
        return convertToResponse(savedBook, null, null, null);
    }

    // ========== GET BOOKS ==========
    @Override
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        return convertToResponse(book, null, null, null);
    }

    @Override
    public BookResponse getBookWithUserAccess(Long bookId, String username, String userType, String department) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
    
        boolean canAccess = canUserAccessBook(book, userType, department, book.getCourseCode());
        BookResponse response = convertToResponse(book, userType, department, book.getCourseCode());
        response.setCanAccess(canAccess);
    
        log.info("User {} can access book {}: {}", username, bookId, canAccess);
        return response;
    }

    @Override
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable)
            .map(book -> convertToResponse(book, null, null, null));
    }

    @Override
    public Page<BookResponse> searchBooks(String keyword, Pageable pageable) {
        return bookRepository.fullTextSearch(keyword, pageable)
                .map(book -> convertToResponse(book, null, null, null));
    }

    @Override
    public Page<BookResponse> getBooksByCategory(String category, Pageable pageable) {
        return bookRepository.findByCategoriesContaining(category, pageable)
                .map(book -> convertToResponse(book, null, null, null));
    }

    @Override
    public Page<BookResponse> getBooksByType(String type, Pageable pageable) {
        BookType bookType = BookType.valueOf(type.toUpperCase());
        return bookRepository.findByType(bookType, pageable)
                .map(book -> convertToResponse(book, null, null, null));
    }

    @Override
    public Page<BookResponse> getBooksByDepartment(String department, Pageable pageable) {
        return bookRepository.findByDepartment(department, pageable)
                .map(book -> convertToResponse(book, null, department, null));
    }

@Override
public Page<BookResponse> getRecommendedBooks(String userType, String department, Pageable pageable) {
    if (department == null || department.trim().isEmpty()) {
        department = "Công nghệ thông tin";
    }
    
    // ✅ Lưu department vào biến final để dùng trong lambda
    final String finalDepartment = department;
    final String finalUserType = userType;
    
    Page<Book> books = bookRepository.findByDepartment(department, pageable);
    return books.map(book -> {
        return convertToResponse(book, finalUserType, finalDepartment, book.getCourseCode());
    });
}

    // ========== UPDATE & DELETE ==========
@Override
public BookResponse updateBook(Long id, CreateBookRequest request) {
    Book book = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

    if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
        book.setTitle(request.getTitle().trim());
    }
    if (request.getAuthor() != null && !request.getAuthor().trim().isEmpty()) {
        book.setAuthor(request.getAuthor().trim());
    }
    if (request.getIsbn() != null) {
        book.setIsbn(request.getIsbn().trim());
    }
    if (request.getDescription() != null) {
        book.setDescription(request.getDescription());
    }
    if (request.getType() != null) {
        book.setType(request.getType());
    }
    if (request.getAccessType() != null) {
        book.setAccessType(request.getAccessType());
    }
    if (request.getPublisher() != null) {
        book.setPublisher(request.getPublisher());
    }
    if (request.getPublicationYear() != null) {
        book.setPublicationYear(request.getPublicationYear());
    }
    if (request.getPages() != null) {
        book.setPages(request.getPages());
    }
    if (request.getTotalPhysicalCopies() != null) {
        book.setTotalPhysicalCopies(request.getTotalPhysicalCopies());
        book.setAvailablePhysicalCopies(request.getTotalPhysicalCopies());
    }
    if (request.getDepartment() != null) {
        book.setDepartment(request.getDepartment());
    }
    if (request.getCourseCode() != null) {
        book.setCourseCode(request.getCourseCode());
    }
    if (request.getCategories() != null) {
        book.setCategories(request.getCategories());
    }
    
    // ✅ THÊM ĐOẠN NÀY - Cập nhật price
    if (request.getPrice() != null) {
        book.setPrice(request.getPrice());
    }

    book.setUpdatedAt(LocalDateTime.now());

    Book updatedBook = bookRepository.save(book);
    log.info("Updated book: {}", updatedBook.getTitle());
    
    return convertToResponse(updatedBook, null, null, null);
}

@Override
public void deleteBook(Long id) {
    Book book = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
    // Xóa cứng (thay thế)
    bookRepository.delete(book);  // ✅ Xóa hẳn khỏi database
    log.info("Hard deleted book: {}", book.getTitle());
}

    // ========== FILE UPLOAD ==========
    @Override
    public String uploadCoverImage(Long bookId, MultipartFile file) {
        try {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found"));
            
            String fileName = "cover_" + bookId + "_" + System.currentTimeMillis() + 
                             getFileExtension(file.getOriginalFilename());
            
            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.createDirectories(fileStorageLocation);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            String fileUrl = "/uploads/" + fileName;
            book.setMainCoverImageUrl(fileUrl);
            bookRepository.save(book);
            
            log.info("Uploaded cover image for book: {}", book.getTitle());
            return fileUrl;
            
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file: " + ex.getMessage());
        }
    }
@Override
public List<Book> getAllBooksEntity() {
    return bookRepository.findAll();
}
    @Override
    public String uploadPdfFile(Long bookId, MultipartFile file) {
        BookResponse response = uploadPdfWithMetadata(bookId, file);
        return response.getPdfFileUrl();
    }

    // ========== PDF WITH METADATA - TÍNH NĂNG CHÍNH ==========
    @Override
    @Transactional
    public BookResponse uploadPdfWithMetadata(Long bookId, MultipartFile file) {
        log.info("Uploading PDF with metadata for book: {}", bookId);
        
        try {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
            
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }
            
            if (!pdfUtil.isValidPdf(file)) {
                throw new RuntimeException("Only PDF files are allowed");
            }
            
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".pdf";
            
            String newFilename = "book_" + bookId + "_" + System.currentTimeMillis() + extension;
            
            Files.createDirectories(fileStorageLocation);
            
            Path targetLocation = fileStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // ĐẾM SỐ TRANG PDF
            Integer pageCount = pdfUtil.countPdfPages(file);
            long fileSize = pdfUtil.getFileSize(file);
            
            String fileUrl = "/uploads/" + newFilename;
            book.setPdfFileUrl(fileUrl);
            
            // CẬP NHẬT SỐ TRANG
            if (pageCount != null && pageCount > 0) {
                book.setPages(pageCount);
                log.info("Updated book pages to: {}", pageCount);
            }
            
            // Cập nhật access type
            if (book.getAccessType() == BookAccessType.PHYSICAL_ONLY) {
                book.setAccessType(BookAccessType.HYBRID);
            } else if (book.getAccessType() == BookAccessType.PREVIEW_ONLY) {
                book.setAccessType(BookAccessType.FULL_DIGITAL);
            }
            
            book.setUpdatedAt(LocalDateTime.now());
            
            Book savedBook = bookRepository.save(book);
            log.info("Uploaded PDF for book: {} - Pages: {}, Size: {} bytes", 
                    savedBook.getTitle(), pageCount, fileSize);
            
            return convertToResponse(savedBook, null, null, null);
            
        } catch (IOException e) {
            log.error("Error uploading PDF: {}", e.getMessage());
            throw new RuntimeException("Could not upload PDF: " + e.getMessage());
        }
    }

    // ========== FIX EXISTING BOOKS - QUAN TRỌNG ==========
    @Override
    @Transactional
    public int updateAllBookPages() {
        log.info("Updating page counts for all books with PDF...");
        
        List<Book> books = bookRepository.findAll();
        int updatedCount = 0;
        
        for (Book book : books) {
            if (book.getPdfFileUrl() != null && !book.getPdfFileUrl().isEmpty()) {
                try {
                    String pdfUrl = book.getPdfFileUrl();
                    String filename = pdfUrl.substring(pdfUrl.lastIndexOf("/") + 1);
                    
                    Path filePath = fileStorageLocation.resolve(filename);
                    
                    if (Files.exists(filePath)) {
                        Integer actualPages = pdfUtil.countPdfPagesFromPath(filePath.toString());
                        
                        if (actualPages != null && !actualPages.equals(book.getPages())) {
                            log.info("Book '{}' - Old pages: {}, Actual pages: {}", 
                                    book.getTitle(), book.getPages(), actualPages);
                            
                            book.setPages(actualPages);
                            book.setUpdatedAt(LocalDateTime.now());
                            bookRepository.save(book);
                            updatedCount++;
                        }
                    } else {
                        log.warn("PDF file not found for book: {} - {}", book.getId(), book.getTitle());
                    }
                } catch (Exception e) {
                    log.error("Error updating pages for book {}: {}", book.getId(), e.getMessage());
                }
            }
        }
        
        log.info("Updated {} books with correct page counts", updatedCount);
        return updatedCount;
    }
    
    @Override
    @Transactional
    public BookResponse updateBookPagesFromPdf(Long bookId) {
        log.info("Updating page count for book: {}", bookId);
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        
        if (book.getPdfFileUrl() == null || book.getPdfFileUrl().isEmpty()) {
            throw new RuntimeException("Book has no PDF file to read pages from");
        }
        
        String pdfUrl = book.getPdfFileUrl();
        String filename = pdfUrl.substring(pdfUrl.lastIndexOf("/") + 1);
        Path filePath = fileStorageLocation.resolve(filename);
        
        if (!Files.exists(filePath)) {
            throw new RuntimeException("PDF file not found on server");
        }
        
        Integer actualPages = pdfUtil.countPdfPagesFromPath(filePath.toString());
        
        if (actualPages != null) {
            int oldPages = book.getPages() != null ? book.getPages() : 0;
            book.setPages(actualPages);
            book.setUpdatedAt(LocalDateTime.now());
            bookRepository.save(book);
            
            log.info("Book '{}' - Pages updated from {} to {}", 
                    book.getTitle(), oldPages, actualPages);
        }
        
        return convertToResponse(book, null, null, null);
    }

    @Override
    public BookResponse updateBookAccessType(Long bookId, String accessType) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        
        BookAccessType newAccessType = BookAccessType.valueOf(accessType.toUpperCase());
        book.setAccessType(newAccessType);
        
        Book updatedBook = bookRepository.save(book);
        log.info("Updated access type for book {} to {}", book.getTitle(), accessType);
        
        return convertToResponse(updatedBook, null, null, null);
    }

    // ========== HELPER METHODS ==========
    @Override
    public boolean canUserAccessBook(Book book, String userType, String department, String courseCode) {
        switch (book.getAccessType()) {
            case FULL_DIGITAL:
            case HYBRID:
                return true;
            case PREVIEW_ONLY:
                return true;
            case PHYSICAL_ONLY:
                return book.getAvailablePhysicalCopies() > 0;
            default:
                return false;
        }
    }

    @Override
    public Book getBookEntityById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
    }

    @Override
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    private BookResponse convertToResponse(Book book, String userType, String department, String courseCode) {
        boolean canReadOnline = (book.getPdfFileUrl() != null && !book.getPdfFileUrl().isEmpty()) && 
                               (book.getAccessType() == BookAccessType.FULL_DIGITAL || 
                                book.getAccessType() == BookAccessType.HYBRID);
        
        boolean canBorrowPhysical = book.getAvailablePhysicalCopies() > 0 && 
                                   (book.getAccessType() == BookAccessType.PHYSICAL_ONLY || 
                                    book.getAccessType() == BookAccessType.HYBRID);
        
        boolean canAccess = canReadOnline || canBorrowPhysical;
        
        if (userType != null && department != null) {
            boolean userSpecificAccess = canUserAccessBook(book, userType, department, courseCode);
            canAccess = canAccess && userSpecificAccess;
        }
        
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .description(book.getDescription())
                .type(book.getType())
                .accessType(book.getAccessType())
                .mainCoverImageUrl(book.getMainCoverImageUrl())
                .coverImageUrl(book.getMainCoverImageUrl())
                .additionalImageUrls(book.getAdditionalImageUrls() != null ? 
                    book.getAdditionalImageUrls() : new ArrayList<>())
                .pdfFileUrl(book.getPdfFileUrl())
                .totalPhysicalCopies(book.getTotalPhysicalCopies())
                .availablePhysicalCopies(book.getAvailablePhysicalCopies())
                .publisher(book.getPublisher())
                .publicationYear(book.getPublicationYear())
                .pages(book.getPages())
                .department(book.getDepartment())
                .courseCode(book.getCourseCode())
                .categories(book.getCategories() != null ? 
                    book.getCategories() : new ArrayList<>())
                .canReadOnline(canReadOnline)
                .canBorrowPhysical(canBorrowPhysical)
                .canAccess(canAccess)
                .createdAt(book.getCreatedAt())
                .build();
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex == -1) return "";
        return fileName.substring(lastIndex);
    }
// book-service/src/main/java/.../service/impl/BookServiceImpl.java
// Thêm các method này vào cuối class, trước dấu }

@Override
public List<BookLocation> searchByLocation(Integer zone, Integer shelf, Integer column, Integer row) {
    log.info("Searching books by location - zone: {}, shelf: {}, column: {}, row: {}", 
             zone, shelf, column, row);
    // TODO: Implement actual search from database
    return new ArrayList<>();
}

@Override
public BookLocation getBookLocation(Long bookId) {
    log.info("Getting location for book: {}", bookId);
    // TODO: Implement get location from database
    return null;
}
// book-service/src/main/java/.../service/impl/BookServiceImpl.java
// Thêm các method mới vào cuối class

@Override
public List<LibraryBranch> getAllBranches() {
    return libraryBranchRepository.findByIsActiveTrue();
}

@Override
public LibraryBranch getBranchById(Long id) {
    return libraryBranchRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Library branch not found: " + id));
}

@Override
public Page<BookResponse> getBooksByBranch(Long branchId, Pageable pageable) {
    return bookRepository.findByLibraryBranchIdAndIsActiveTrue(branchId, pageable)
            .map(book -> convertToResponse(book, null, null, null));
}

@Override
public List<String> getAvailableLocations(Long bookId, Long branchId) {
    Book book = getBookEntityById(bookId);
    List<String> locations = new ArrayList<>();
    
    if (book.getLocationPrefix() != null && book.getAvailablePhysicalCopies() > 0) {
        for (int i = 1; i <= book.getAvailablePhysicalCopies(); i++) {
            locations.add(book.getLocationPrefix() + i);
        }
    }
    
    return locations;
}
}
