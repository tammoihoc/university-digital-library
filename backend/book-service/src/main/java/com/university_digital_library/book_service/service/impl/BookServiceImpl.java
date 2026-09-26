// book-service/src/main/java/com/university_digital_library/book_service/service/impl/BookServiceImpl.java
package com.university_digital_library.book_service.service.impl;

import com.university_digital_library.book_service.dto.BookDTO;
import com.university_digital_library.book_service.dto.CreateBookRequest;
import com.university_digital_library.book_service.model.Book;
import com.university_digital_library.book_service.repository.BookRepository;
import com.university_digital_library.book_service.service.BookService;
import com.university_digital_library.book_service.util.PdfUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final PdfUtil pdfUtil;
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    @Override
    @Transactional
    public BookDTO createBook(CreateBookRequest request) {
        // Validate
        if (request.getIsbn() != null && !request.getIsbn().isEmpty()) {
            bookRepository.findByIsbn(request.getIsbn())
                .ifPresent(b -> { throw new RuntimeException("ISBN already exists"); });
        }

        Book book = Book.builder()
            .title(request.getTitle())
            .author(request.getAuthor())
            .isbn(request.getIsbn())
            .description(request.getDescription())
            .type(request.getType())
            .accessType(request.getAccessType())
            .publisher(request.getPublisher())
            .publicationYear(request.getPublicationYear())
            .pages(request.getPages())
            .language(request.getLanguage())
            .department(request.getDepartment())
            .courseCode(request.getCourseCode())
            .categories(request.getCategories() != null ? request.getCategories() : new ArrayList<>())
            .price(request.getPrice() != null ? request.getPrice() : 0.0)
            .totalPhysicalCopies(request.getTotalPhysicalCopies() != null ? request.getTotalPhysicalCopies() : 0)
            .availablePhysicalCopies(request.getTotalPhysicalCopies() != null ? request.getTotalPhysicalCopies() : 0)
            .libraryBranchId(request.getLibraryBranchId() != null ? request.getLibraryBranchId() : 1L)
            .isActive(true)
            .build();

        Book saved = bookRepository.save(book);
        log.info("Created book: {} with ID: {}", saved.getTitle(), saved.getId());

        return BookDTO.fromEntity(saved);
    }

    @Override
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Book not found: " + id));
        return BookDTO.fromEntity(book);
    }

    @Override
    public Page<BookDTO> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable)
            .map(BookDTO::fromEntity);
    }

    @Override
    public Page<BookDTO> searchBooks(String keyword, Pageable pageable) {
        return bookRepository.fullTextSearch(keyword, pageable)
            .map(BookDTO::fromEntity);
    }

    @Override
    public Page<BookDTO> getBooksByCategory(String category, Pageable pageable) {
        return bookRepository.findByCategoriesContaining(category, pageable)
            .map(BookDTO::fromEntity);
    }

    @Override
    public Page<BookDTO> getBooksByDepartment(String department, Pageable pageable) {
        return bookRepository.findByDepartment(department, pageable)
            .map(BookDTO::fromEntity);
    }

    @Override
    @Transactional
    public BookDTO updateBook(Long id, CreateBookRequest request) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Book not found: " + id));

        // Cập nhật các trường cơ bản
        if (request.getTitle() != null) book.setTitle(request.getTitle());
        if (request.getAuthor() != null) book.setAuthor(request.getAuthor());
        if (request.getDescription() != null) book.setDescription(request.getDescription());
        if (request.getType() != null) book.setType(request.getType());
        if (request.getAccessType() != null) book.setAccessType(request.getAccessType());
        if (request.getPublisher() != null) book.setPublisher(request.getPublisher());
        if (request.getPublicationYear() != null) book.setPublicationYear(request.getPublicationYear());
        if (request.getPages() != null) book.setPages(request.getPages());
        if (request.getLanguage() != null) book.setLanguage(request.getLanguage());
        if (request.getDepartment() != null) book.setDepartment(request.getDepartment());
        if (request.getCourseCode() != null) book.setCourseCode(request.getCourseCode());
        if (request.getCategories() != null) book.setCategories(request.getCategories());
        if (request.getPrice() != null) book.setPrice(request.getPrice());
        if (request.getTotalPhysicalCopies() != null) {
            int diff = request.getTotalPhysicalCopies() - book.getTotalPhysicalCopies();
            book.setTotalPhysicalCopies(request.getTotalPhysicalCopies());
            book.setAvailablePhysicalCopies(book.getAvailablePhysicalCopies() + diff);
        }

        // Xử lý ISBN
        if (request.getIsbn() != null && !request.getIsbn().equals(book.getIsbn())) {
            bookRepository.findByIsbn(request.getIsbn())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new RuntimeException("ISBN already exists");
                    }
                });
            book.setIsbn(request.getIsbn());
        }

        // Xử lý libraryBranchId
        if (request.getLibraryBranchId() != null) {
            // Nếu muốn kiểm tra branch tồn tại, inject LibraryBranchRepository
            // if (!libraryBranchRepository.existsById(request.getLibraryBranchId())) {
            //     throw new RuntimeException("Library branch not found: " + request.getLibraryBranchId());
            // }
            book.setLibraryBranchId(request.getLibraryBranchId());
        }

        book.setUpdatedAt(LocalDateTime.now());

        try {
            Book updated = bookRepository.save(book);
            return BookDTO.fromEntity(updated);
        } catch (Exception e) {
            log.error("Error updating book: {}", e.getMessage(), e);
            throw new RuntimeException("Update failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Book not found: " + id));
        bookRepository.delete(book);
        log.info("Deleted book: {}", book.getTitle());
    }

    @Override
    public String uploadCoverImage(Long id, MultipartFile file) {
        try {
            Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found: " + id));

            String filename = "cover_" + id + "_" + System.currentTimeMillis() +
                getFileExtension(file.getOriginalFilename());

            Files.createDirectories(fileStorageLocation);
            Path targetLocation = fileStorageLocation.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/uploads/" + filename;
            book.setCoverImageUrl(fileUrl);
            bookRepository.save(book);

            return fileUrl;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload cover image: " + e.getMessage());
        }
    }
@Override
@Transactional
public String uploadPdfFile(Long id, MultipartFile file) {
    try {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Book not found: " + id));

        String filename = "book_" + id + "_" + System.currentTimeMillis() + ".pdf";
        Files.createDirectories(fileStorageLocation);
        Path targetLocation = fileStorageLocation.resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        String fileUrl = "/uploads/" + filename;
        book.setPdfFileUrl(fileUrl);

        // Đếm số trang từ file đã lưu
        Integer pageCount = pdfUtil.countPdfPagesFromPath(targetLocation.toString());
        log.info("📄 PDF page count from saved file: {}", pageCount);
        
        if (pageCount != null && pageCount > 0) {
            book.setPages(pageCount);
            log.info("✅ Đã set pages = {}", pageCount);
        } else {
            log.warn("⚠️ Không đếm được số trang, giữ nguyên pages = {}", book.getPages());
        }

        if (book.getAccessType() == Book.BookAccessType.PHYSICAL_ONLY) {
            book.setAccessType(Book.BookAccessType.HYBRID);
        }

        // *** PHẢI CÓ DÒNG NÀY ĐỂ LƯU VÀO DB ***
        bookRepository.save(book);
        log.info("✅ Book saved with PDF: {}", fileUrl);

        return fileUrl;

    } catch (IOException e) {
        throw new RuntimeException("Failed to upload PDF: " + e.getMessage());
    }
}

    @Override
    @Transactional
    public BookDTO updateAvailableCopies(Long id, Integer newAvailable) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Book not found: " + id));

        if (newAvailable < 0 || newAvailable > book.getTotalPhysicalCopies()) {
            throw new RuntimeException("Invalid available copies count");
        }

        book.setAvailablePhysicalCopies(newAvailable);
        Book updated = bookRepository.save(book);

        return BookDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public boolean decrementAvailableCopies(Long id) {
        int updated = bookRepository.decrementAvailableCopies(id);
        if (updated == 0) {
            log.warn("⚠️ Không thể giảm số lượng sách {}: đã hết hoặc không tồn tại", id);
        }
        return updated > 0;
    }

    @Override
    @Transactional
    public boolean incrementAvailableCopies(Long id) {
        int updated = bookRepository.incrementAvailableCopies(id);
        if (updated == 0) {
            log.warn("⚠️ Không thể tăng số lượng sách {}: đã đạt totalPhysicalCopies hoặc không tồn tại", id);
        }
        return updated > 0;
    }

    @Override
    public Book getBookEntity(Long id) {
        return bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Book not found: " + id));
    }

    @Override
    public boolean canUserAccessBook(Long bookId, String userType, String department) {
        Book book = getBookEntity(bookId);

        if (book.getAccessType() == Book.BookAccessType.FULL_DIGITAL ||
            book.getAccessType() == Book.BookAccessType.HYBRID) {
            return true;
        }

        if (book.getAccessType() == Book.BookAccessType.PREVIEW_ONLY) {
            return true;
        }

        if (book.getAccessType() == Book.BookAccessType.PHYSICAL_ONLY) {
            return book.getAvailablePhysicalCopies() > 0;
        }

        return false;
    }

    @Override
    public Integer countPdfPages(Long bookId) {
        Book book = getBookEntity(bookId);
        if (book.getPdfFileUrl() == null) {
            return null;
        }
        String filename = book.getPdfFileUrl().substring(book.getPdfFileUrl().lastIndexOf("/") + 1);
        Path filePath = Paths.get("uploads").resolve(filename).normalize();
        if (!Files.exists(filePath)) {
            return null;
        }
        try {
            return pdfUtil.countPdfPagesFromPath(filePath.toString());
        } catch (Exception e) {
            log.error("Error counting PDF pages for book {}: {}", bookId, e.getMessage());
            return null;
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex == -1) return "";
        return fileName.substring(lastIndex);
    }
}
