// book-service/src/main/java/com/university_digital_library/book_service/controller/BookController.java
package com.university_digital_library.book_service.controller;

import com.university_digital_library.book_service.dto.BookDTO;
import com.university_digital_library.book_service.dto.CreateBookRequest;
import com.university_digital_library.book_service.model.Book;
import com.university_digital_library.book_service.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.university_digital_library.book_service.model.LibraryBranch;
import com.university_digital_library.book_service.repository.LibraryBranchRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import java.util.List;
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {
    
    private final BookService bookService;
    
    private final LibraryBranchRepository libraryBranchRepository;   // <-- THÊM DÒNG NÀY
    // ========== PUBLIC ENDPOINTS ==========
    
    @GetMapping
    public ResponseEntity<Page<BookDTO>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(bookService.getAllBooks(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<BookDTO>> searchBooks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookService.searchBooks(keyword, pageable));
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<BookDTO>> getBooksByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookService.getBooksByCategory(category, pageable));
    }
    
    @GetMapping("/department/{department}")
    public ResponseEntity<Page<BookDTO>> getBooksByDepartment(
            @PathVariable String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookService.getBooksByDepartment(department, pageable));
    }
    
    @GetMapping("/{id}/availability")
    public ResponseEntity<Map<String, Object>> getBookAvailability(@PathVariable Long id) {
        BookDTO book = bookService.getBookById(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("bookId", book.getId());
        response.put("title", book.getTitle());
        response.put("availablePhysicalCopies", book.getAvailablePhysicalCopies());
        response.put("totalPhysicalCopies", book.getTotalPhysicalCopies());
        response.put("canBeBorrowed", book.getCanBorrowPhysical());
        response.put("canReadOnline", book.getCanReadOnline());
        response.put("author", book.getAuthor());
        response.put("accessType", book.getAccessType());
        
        return ResponseEntity.ok(response);
    }

    // ========== PDF ENDPOINT ==========
    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> getPdf(@PathVariable Long id, Authentication auth) {
        // 1. Lấy thông tin sách
        Book book = bookService.getBookEntity(id);
        if (book == null || book.getPdfFileUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        // 2. Kiểm tra quyền
        String userType = getUserTypeFromAuth(auth);
        String department = getDepartmentFromAuth(auth);
        boolean canAccess = bookService.canUserAccessBook(book.getId(), userType, department);
        if (!canAccess) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 3. Lấy đường dẫn file PDF
        String pdfUrl = book.getPdfFileUrl();
        String filename = pdfUrl.substring(pdfUrl.lastIndexOf("/") + 1);
        Path filePath = Paths.get("uploads").resolve(filename).normalize();

        // 4. Kiểm tra file tồn tại
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        // 5. Trả về file
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error serving PDF: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========== PROTECTED ENDPOINTS (ADMIN/LIBRARIAN) ==========
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody CreateBookRequest request) {
        BookDTO book = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BookDTO> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody CreateBookRequest request) {
        
        BookDTO book = bookService.updateBook(id, request);
        return ResponseEntity.ok(book);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/upload-cover")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<String> uploadCover(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        
        String url = bookService.uploadCoverImage(id, file);
        return ResponseEntity.ok(url);
    }
    
    @PostMapping("/{id}/upload-pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<String> uploadPdf(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        
        String url = bookService.uploadPdfFile(id, file);
        return ResponseEntity.ok(url);
    }
    
    // ⚠️ Endpoint này trước đây KHÔNG có @PreAuthorize — chỉ cần isAuthenticated()
    // từ SecurityConfig mặc định, nghĩa là BẤT KỲ user đã đăng nhập nào (kể cả STUDENT)
    // đều có thể tự ý set availablePhysicalCopies của bất kỳ sách nào. Đã bổ sung phân quyền.
    // Khuyến khích dùng /increment và /decrement bên dưới thay vì set trực tiếp một số tuyệt đối,
    // vì set tuyệt đối vẫn có race condition khi có 2 request đọc-rồi-ghi cùng lúc.
    @PostMapping("/{id}/update-available")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BookDTO> updateAvailableCopies(
            @PathVariable Long id,
            @RequestParam Integer newAvailable) {
        
        BookDTO book = bookService.updateAvailableCopies(id, newAvailable);
        return ResponseEntity.ok(book);
    }

    // Tăng/giảm nguyên tử ở DB — không bị race condition dù nhiều request cùng lúc.
    // borrow-service/fine_service nên gọi 2 endpoint này thay vì đọc số lượng rồi tự tính.
    @PostMapping("/{id}/decrement-available")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Object>> decrementAvailableCopies(@PathVariable Long id) {
        boolean success = bookService.decrementAvailableCopies(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        if (!success) {
            response.put("message", "Không còn bản sao nào để giảm (đã hết sách hoặc sách không tồn tại)");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/increment-available")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Object>> incrementAvailableCopies(@PathVariable Long id) {
        boolean success = bookService.incrementAvailableCopies(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        if (!success) {
            response.put("message", "Không thể tăng thêm (đã đạt totalPhysicalCopies hoặc sách không tồn tại)");
        }
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}/pdf-info")
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
public ResponseEntity<Map<String, Object>> getPdfInfo(@PathVariable Long id) {
    Book book = bookService.getBookEntity(id);
    Map<String, Object> info = new HashMap<>();
    info.put("hasPdf", book.getPdfFileUrl() != null);
    info.put("pdfUrl", book.getPdfFileUrl());
    info.put("pages", book.getPages());
    info.put("pageCountFromFile", bookService.countPdfPages(id));
    return ResponseEntity.ok(info);
}
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Book Service is healthy! 📚");
    }

    // ========== HELPER METHODS ==========
    
    private String getUserTypeFromAuth(Authentication auth) {
        if (auth == null) return "STUDENT";
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("STUDENT");
    }
    
    private String getDepartmentFromAuth(Authentication auth) {
        // Tạm thời trả về mặc định. Nếu có thông tin department từ JWT thì lấy.
        // Hoặc có thể lấy từ database nếu cần.
        return "Công nghệ thông tin";
    }
@GetMapping("/branches")
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
public ResponseEntity<List<LibraryBranch>> getAllBranches() {
    return ResponseEntity.ok(libraryBranchRepository.findByIsActiveTrue());
}
}
