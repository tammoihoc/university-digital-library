package com.university_digital_library.book_service.controller;

import com.university_digital_library.book_service.model.LibraryBranch;
import com.university_digital_library.book_service.dto.request.CreateBookRequest;
import com.university_digital_library.book_service.dto.response.BookResponse;
import com.university_digital_library.book_service.model.Book;
import com.university_digital_library.book_service.model.BookAccessType;
import com.university_digital_library.book_service.service.BookService;
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
import com.university_digital_library.book_service.model.BookLocation;
import com.university_digital_library.book_service.repository.BookLocationRepository;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;
    private final BookLocationRepository bookLocationRepository;

    // ========== PUBLIC ENDPOINTS ==========
    
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        log.info("Fetching book with id: {}", id);
        BookResponse response = bookService.getBookById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<BookResponse>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<BookResponse> response = bookService.getAllBooks(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BookResponse>> searchBooks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponse> response = bookService.searchBooks(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<BookResponse>> getBooksByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponse> response = bookService.getBooksByCategory(category, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Page<BookResponse>> getBooksByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponse> response = bookService.getBooksByType(type, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<Page<BookResponse>> getBooksByDepartment(
            @PathVariable String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponse> response = bookService.getBooksByDepartment(department, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommended")
    public ResponseEntity<Page<BookResponse>> getRecommendedBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        
        String userType = getUserTypeFromAuth(auth);
        String department = getDepartmentFromAuth(auth);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponse> response = bookService.getRecommendedBooks(userType, department, pageable);
        return ResponseEntity.ok(response);
    }

    // ========== PROTECTED ENDPOINTS ==========
    
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getBookPdf(
            @PathVariable Long id,
            Authentication auth) {
        
        try {
            String username = auth.getName();
            String userType = getUserTypeFromAuth(auth);
            String department = getDepartmentFromAuth(auth);
            
            log.info("User {} (type: {}) requesting PDF for book {}", username, userType, id);

            Book book = bookService.getBookEntityById(id);
            if (book == null || book.getPdfFileUrl() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Book or PDF not found".getBytes());
            }
            
            boolean canAccess = bookService.canUserAccessBook(book, userType, department, book.getCourseCode());
            if (!canAccess) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You don't have permission to access this PDF".getBytes());
            }

            String pdfUrl = book.getPdfFileUrl();
            String filename = pdfUrl.substring(pdfUrl.lastIndexOf("/") + 1);

            Path uploadsDir = Paths.get("uploads").toAbsolutePath();
            Path filePath = uploadsDir.resolve(filename);
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(("PDF not found: " + filename).getBytes());
            }

            byte[] pdfBytes = Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentLength(pdfBytes.length);
            headers.set("Content-Disposition", "inline; filename=\"" + filename + "\"");
            headers.setCacheControl("public, max-age=3600");

            log.info("PDF served successfully to user {}: {}", username, filename);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error getting PDF: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/{id}/access")
    public ResponseEntity<BookResponse> getBookWithAccess(
            @PathVariable Long id,
            Authentication auth) {
        
        String username = auth.getName();
        String userType = getUserTypeFromAuth(auth);
        String department = getDepartmentFromAuth(auth);
        
        log.info("Fetching book {} with access check for user: {}", id, username);
        BookResponse response = bookService.getBookWithUserAccess(id, username, userType, department);
        return ResponseEntity.ok(response);
    }

    // ========== ADMIN/LIBRARIAN ENDPOINTS ==========
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BookResponse> createBook(@RequestBody CreateBookRequest request) {
        log.info("Creating new book: {}", request.getTitle());
        BookResponse response = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @RequestBody CreateBookRequest request) {
        
        log.info("Updating book with id: {}", id);
        BookResponse response = bookService.updateBook(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        log.info("Deleting book with id: {}", id);
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/upload-cover")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<String> uploadCoverImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        
        log.info("Uploading cover image for book id: {}", id);
        String fileUrl = bookService.uploadCoverImage(id, file);
        return ResponseEntity.ok(fileUrl);
    }

    // ========== PDF UPLOAD WITH METADATA ==========
    
    @PostMapping("/{id}/upload-pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Object>> uploadPdfWithMetadata(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        
        log.info("Uploading PDF with metadata for book: {}", id);
        
        BookResponse response = bookService.uploadPdfWithMetadata(id, file);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "PDF uploaded successfully");
        result.put("bookId", response.getId());
        result.put("title", response.getTitle());
        result.put("pages", response.getPages());
        result.put("pdfUrl", response.getPdfFileUrl());
        
        return ResponseEntity.ok(result);
    }
    
    // ========== FIX EXISTING BOOKS ==========
    
    @PostMapping("/update-all-pages")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Object>> updateAllBookPages() {
        log.info("Admin triggered: updating all book pages");
        
        int updatedCount = bookService.updateAllBookPages();
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Updated all books with correct page counts");
        result.put("updatedCount", updatedCount);
        result.put("status", "SUCCESS");
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/{id}/update-pages")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Object>> updateBookPages(@PathVariable Long id) {
        log.info("Updating pages from PDF for book: {}", id);
        
        BookResponse response = bookService.updateBookPagesFromPdf(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Book pages updated from PDF");
        result.put("bookId", response.getId());
        result.put("title", response.getTitle());
        result.put("newPages", response.getPages());
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}/pdf-info")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Object>> getPdfInfo(@PathVariable Long id) {
        Book book = bookService.getBookEntityById(id);
        
        Map<String, Object> info = new HashMap<>();
        info.put("bookId", book.getId());
        info.put("title", book.getTitle());
        info.put("hasPdf", book.getPdfFileUrl() != null);
        info.put("pdfUrl", book.getPdfFileUrl());
        info.put("pages", book.getPages());
        info.put("accessType", book.getAccessType());
        
        return ResponseEntity.ok(info);
    }

    @PatchMapping("/{id}/access-type")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BookResponse> updateAccessType(
            @PathVariable Long id,
            @RequestParam String accessType) {
        
        log.info("Updating access type for book id: {} to {}", id, accessType);
        BookResponse response = bookService.updateBookAccessType(id, accessType);
        return ResponseEntity.ok(response);
    }

    // ✅ API CHO BORROW SERVICE
    @GetMapping("/{id}/availability")
    public ResponseEntity<Map<String, Object>> getBookAvailability(@PathVariable Long id) {
        log.info("Getting availability for book id: {}", id);
        
        BookResponse bookResponse = bookService.getBookById(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("bookId", bookResponse.getId());
        response.put("title", bookResponse.getTitle());
        response.put("availablePhysicalCopies", bookResponse.getAvailablePhysicalCopies());
        response.put("canBeBorrowed", bookResponse.isCanBorrowPhysical());
        response.put("totalPhysicalCopies", bookResponse.getTotalPhysicalCopies());
        response.put("author", bookResponse.getAuthor());
        response.put("accessType", bookResponse.getAccessType().toString());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/update-available")
    public ResponseEntity<BookResponse> updateAvailableCopies(
            @PathVariable Long id,
            @RequestParam Integer newAvailable) {
        
        log.info("Updating available copies for book {} to {}", id, newAvailable);
        
        try {
            Book book = bookService.getBookEntityById(id);
            
            if (newAvailable < 0 || newAvailable > book.getTotalPhysicalCopies()) {
                return ResponseEntity.badRequest().body(null);
            }
            
            book.setAvailablePhysicalCopies(newAvailable);
            
            if (newAvailable > 0 && book.getAccessType() == BookAccessType.FULL_DIGITAL) {
                book.setAccessType(BookAccessType.HYBRID);
            }
            
            bookService.saveBook(book);
            
            BookResponse response = bookService.getBookById(id);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Error updating available copies: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ========== LOCATION API ==========
    
    @GetMapping("/{id}/location")
    public ResponseEntity<Map<String, Object>> getBookLocation(@PathVariable Long id) {
        log.info("Getting location for book: {}", id);
        
        try {
            List<BookLocation> locations = bookLocationRepository.findByBookId(id);
            Book book = bookService.getBookEntityById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("bookId", book.getId());
            response.put("title", book.getTitle());
            
            if (locations != null && !locations.isEmpty()) {
                BookLocation location = locations.get(0);
                response.put("fullCode", location.getFullCode());
                response.put("zone", location.getZone());
                response.put("zoneName", location.getZoneName());
                response.put("shelf", location.getShelf());
                response.put("shelfName", location.getShelfName());
                response.put("column", location.getColNum());
                response.put("columnName", location.getColName());
                response.put("row", location.getRowNum());
                response.put("rowName", location.getRowName());
                response.put("position", location.getPosition());
                response.put("totalCopies", locations.size());
                
                String building = location.getZone() >= 600 ? "E" : "B";
                response.put("building", building);
                response.put("buildingName", building.equals("B") ? 
                    "B - 475A Điện Biên Phủ, P.25, Q.Bình Thạnh" : 
                    "E - Khu Công nghệ cao, Quận 9");
            } else {
                response.put("fullCode", "Chưa có vị trí");
                response.put("totalCopies", 0);
                response.put("building", "B");
                response.put("buildingName", "B - 475A Điện Biên Phủ, P.25, Q.Bình Thạnh");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting location: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/locations/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Object>> getAllBookLocations() {
        log.info("Getting all book locations");
        
        List<BookLocation> allLocations = bookLocationRepository.findAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalLocations", allLocations.size());
        
        List<Map<String, Object>> locations = new ArrayList<>();
        for (BookLocation loc : allLocations) {
            Map<String, Object> locInfo = new HashMap<>();
            if (loc.getBook() != null) {
                locInfo.put("bookId", loc.getBook().getId());
                locInfo.put("title", loc.getBook().getTitle());
            }
            locInfo.put("fullCode", loc.getFullCode());
            locInfo.put("zone", loc.getZone());
            locInfo.put("zoneName", loc.getZoneName());
            locInfo.put("shelf", loc.getShelf());
            locInfo.put("shelfName", loc.getShelfName());
            locInfo.put("column", loc.getColNum());
            locInfo.put("columnName", loc.getColName());
            locInfo.put("row", loc.getRowNum());
            locInfo.put("rowName", loc.getRowName());
            locInfo.put("position", loc.getPosition());
            locInfo.put("building", loc.getZone() != null && loc.getZone() >= 600 ? "E" : "B");
            locations.add(locInfo);
        }
        
        response.put("locations", locations);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bookId}/locations/all")
    public ResponseEntity<Map<String, Object>> getAllLocationsForBook(@PathVariable Long bookId) {
        log.info("Getting all locations for book: {}", bookId);
        
        Book book = bookService.getBookEntityById(bookId);
        List<BookLocation> locations = bookLocationRepository.findByBookId(bookId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("bookId", book.getId());
        response.put("title", book.getTitle());
        response.put("totalCopies", book.getTotalPhysicalCopies());
        response.put("locationsCount", locations.size());
        
        List<Map<String, Object>> locationList = new ArrayList<>();
        for (BookLocation loc : locations) {
            Map<String, Object> locInfo = new HashMap<>();
            locInfo.put("fullCode", loc.getFullCode());
            locInfo.put("zone", loc.getZone());
            locInfo.put("zoneName", loc.getZoneName());
            locInfo.put("shelf", loc.getShelf());
            locInfo.put("shelfName", loc.getShelfName());
            locInfo.put("column", loc.getColNum());
            locInfo.put("columnName", loc.getColName());
            locInfo.put("row", loc.getRowNum());
            locInfo.put("rowName", loc.getRowName());
            locInfo.put("position", loc.getPosition());
            locInfo.put("building", loc.getZone() != null && loc.getZone() >= 600 ? "E" : "B");
            locationList.add(locInfo);
        }
        
        response.put("locations", locationList);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/locations/generate-all")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Object>> generateAllBookLocations() {
        log.info("Generating locations for all books...");
        
        List<Book> books = bookService.getAllBooksEntity();
        int generated = 0;
        int totalLocations = 0;
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (Book book : books) {
            try {
                int availableCopies = book.getAvailablePhysicalCopies() != null ? book.getAvailablePhysicalCopies() : 0;
                log.info("Book: {} - Available copies: {}", book.getTitle(), availableCopies);
                
                if (availableCopies == 0) {
                    log.info("Skipping book {} - no available copies", book.getTitle());
                    continue;
                }
                
                List<BookLocation> existing = bookLocationRepository.findByBookId(book.getId());
                if (!existing.isEmpty()) {
                    bookLocationRepository.deleteAll(existing);
                    log.info("Deleted {} existing locations for book {}", existing.size(), book.getId());
                }
                
                for (int i = 1; i <= availableCopies; i++) {
                    BookLocation location = generateLocationForBook(book, i);
                    bookLocationRepository.save(location);
                    totalLocations++;
                    log.info("Created location {} for book {} (copy {}/{})", 
                             location.getFullCode(), book.getTitle(), i, availableCopies);
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("bookId", book.getId());
                result.put("title", book.getTitle());
                result.put("availableCopies", availableCopies);
                result.put("locationsCreated", availableCopies);
                results.add(result);
                generated++;
                
            } catch (Exception e) {
                log.error("Error generating location for book {}: {}", book.getId(), e.getMessage());
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Location generation completed");
        response.put("totalBooks", books.size());
        response.put("generated", generated);
        response.put("totalLocationsCreated", totalLocations);
        response.put("details", results);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookId}/locations/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Object>> generateLocationsForBook(@PathVariable Long bookId) {
        log.info("Generating locations for book: {}", bookId);
        
        Book book = bookService.getBookEntityById(bookId);
        int availableCopies = book.getAvailablePhysicalCopies() != null ? book.getAvailablePhysicalCopies() : 0;
        
        if (availableCopies == 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "No available copies to generate locations"));
        }
        
        List<BookLocation> existingLocations = bookLocationRepository.findByBookId(bookId);
        if (!existingLocations.isEmpty()) {
            bookLocationRepository.deleteAll(existingLocations);
            log.info("Deleted {} existing locations for book {}", existingLocations.size(), bookId);
        }
        
        List<Map<String, Object>> locations = new ArrayList<>();
        
        for (int i = 1; i <= availableCopies; i++) {
            BookLocation location = generateLocationForBook(book, i);
            bookLocationRepository.save(location);
            
            Map<String, Object> locInfo = new HashMap<>();
            locInfo.put("copyNumber", i);
            locInfo.put("fullCode", location.getFullCode());
            locInfo.put("location", String.format("Khu %d - Kệ %d - Cột %d - Hàng %d - Vị trí %d",
                location.getZone(), location.getShelf(), location.getColNum(), location.getRowNum(), location.getPosition()));
            locInfo.put("building", location.getZone() >= 600 ? "E" : "B");
            locations.add(locInfo);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("bookId", book.getId());
        response.put("title", book.getTitle());
        response.put("availableCopies", availableCopies);
        response.put("locationsCount", locations.size());
        response.put("locations", locations);
        
        return ResponseEntity.ok(response);
    }
    
    private BookLocation generateLocationForBook(Book book, int copyNumber) {
        int zone = determineZoneFromBook(book);
        int shelf = determineShelfFromBook(book);
        int column = determineColumnFromBook(book);
        int row = determineRowFromBook(book);
        
        int position = copyNumber;
        int currentRow = row;
        int currentColumn = column;
        
        if (position > 9) {
            int extraRows = (position - 1) / 9;
            position = ((position - 1) % 9) + 1;
            currentRow = row + extraRows;
            
            if (currentRow > 9) {
                int extraCols = (currentRow - 1) / 9;
                currentColumn = column + extraCols;
                currentRow = ((currentRow - 1) % 9) + 1;
            }
        }
        
        if (currentColumn > 9) currentColumn = 9;
        if (currentRow > 9) currentRow = 9;
        
        String fullCode = String.format("%d%d%d.%d%d", 
            zone / 100, shelf, currentColumn, currentRow, position);
        
        BookLocation location = new BookLocation();
        location.setBook(book);
        location.setCopyNumber(copyNumber);
        location.setZone(zone);
        location.setShelf(shelf);
        location.setColNum(currentColumn);
        location.setRowNum(currentRow);
        location.setPosition(position);
        location.setFullCode(fullCode);
        location.setIsAvailable(true);
        location.setZoneName(getZoneName(zone));
        location.setShelfName(getShelfName(shelf));
        location.setColName(getColumnName(currentColumn));
        location.setRowName(getRowName(currentRow));
        
        return location;
    }
    
    private int determineZoneFromBook(Book book) {
        String title = book.getTitle().toLowerCase();
        String dept = book.getDepartment() != null ? book.getDepartment().toLowerCase() : "";
        
        if (title.contains("công nghệ") || title.contains("programming") || dept.contains("công nghệ") || dept.contains("it")) {
            return 600;
        }
        if (title.contains("tâm lý") || dept.contains("tâm lý")) {
            return 300;
        }
        if (title.contains("văn học") || title.contains("fiction") || dept.contains("văn")) {
            return 800;
        }
        if (title.contains("kinh doanh") || title.contains("giàu") || dept.contains("kinh tế")) {
            return 600;
        }
        if (title.contains("ngôn ngữ") || title.contains("tiếng")) {
            return 400;
        }
        return 300;
    }
    
    private int determineShelfFromBook(Book book) {
        String title = book.getTitle().toLowerCase();
        
        if (title.contains("đắc nhân tâm")) return 1;
        if (title.contains("nghĩ giàu")) return 2;
        if (title.contains("pho mát")) return 3;
        if (title.contains("con bò")) return 4;
        if (title.contains("chiến thắng")) return 5;
        if (title.contains("test")) return 6;
        return 1;
    }
    
    private int determineColumnFromBook(Book book) {
        String title = book.getTitle().toLowerCase();
        
        if (title.contains("nhân tâm") || title.contains("giao tiếp")) return 2;
        if (title.contains("giàu") || title.contains("kinh doanh")) return 1;
        if (title.contains("thay đổi")) return 2;
        if (title.contains("triết lý")) return 3;
        if (title.contains("tâm lý")) return 1;
        return 1;
    }
    
    private int determineRowFromBook(Book book) {
        String title = book.getTitle().toLowerCase();
        
        if (title.contains("cơ bản")) return 1;
        if (title.contains("nâng cao")) return 3;
        return 2;
    }

    // ========== UTILITY ==========
    
    private String getUserTypeFromAuth(Authentication auth) {
        if (auth == null) return "STUDENT";
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("STUDENT");
    }
    
    private String getDepartmentFromAuth(Authentication auth) {
        return "Công nghệ thông tin";
    }
    
    @GetMapping("/uploads/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> servePdfFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("uploads").resolve(filename).normalize();
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                        .body(resource);
            }
            
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            
        } catch (Exception e) {
            log.error("Error serving PDF file: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Book Service is healthy! 📚");
    }

    @GetMapping("/location/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<BookLocation>> searchByLocation(
            @RequestParam(required = false) Integer zone,
            @RequestParam(required = false) Integer shelf,
            @RequestParam(required = false) Integer column,
            @RequestParam(required = false) Integer row) {
        
        log.info("Searching books by location: zone={}, shelf={}, column={}, row={}", 
                 zone, shelf, column, row);
        
        List<BookLocation> locations = bookService.searchByLocation(zone, shelf, column, row);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}/price")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Object>> getBookPrice(@PathVariable Long id) {
        log.info("Getting price for book: {}", id);
        
        Book book = bookService.getBookEntityById(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("bookId", book.getId());
        response.put("title", book.getTitle());
        response.put("price", book.getPrice() != null ? book.getPrice() : 0.0);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/branches")
    public ResponseEntity<List<LibraryBranch>> getAllBranches() {
        log.info("Getting all library branches");
        List<LibraryBranch> branches = bookService.getAllBranches();
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/branches/{branchId}")
    public ResponseEntity<LibraryBranch> getBranchById(@PathVariable Long branchId) {
        log.info("Getting library branch: {}", branchId);
        LibraryBranch branch = bookService.getBranchById(branchId);
        return ResponseEntity.ok(branch);
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<Page<BookResponse>> getBooksByBranch(
            @PathVariable Long branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching books for branch: {}", branchId);
        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponse> response = bookService.getBooksByBranch(branchId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/available-locations")
    public ResponseEntity<Map<String, Object>> getAvailableLocations(
            @PathVariable Long id,
            @RequestParam(required = false) Long branchId) {
        
        log.info("Getting available locations for book: {}, branch: {}", id, branchId);
        
        Book book = bookService.getBookEntityById(id);
        int availableCopies = book.getAvailablePhysicalCopies();
        
        List<String> locations = new ArrayList<>();
        
        if (availableCopies > 0 && book.getLocationPrefix() != null) {
            for (int i = 1; i <= availableCopies; i++) {
                locations.add(book.getLocationPrefix() + i);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("bookId", book.getId());
        response.put("title", book.getTitle());
        response.put("availableCopies", availableCopies);
        response.put("locations", locations);
        response.put("locationHint", "Sách được sắp xếp theo thứ tự tăng dần, hãy lấy cuốn có số nhỏ nhất trước");
        
        return ResponseEntity.ok(response);
    }

    // Helper methods cho location names
    private String getZoneName(int zone) {
        switch(zone) {
            case 100: return "Triết học & Tâm lý học";
            case 200: return "Tôn giáo";
            case 300: return "Khoa học xã hội";
            case 400: return "Ngôn ngữ";
            case 500: return "Khoa học tự nhiên";
            case 600: return "Công nghệ";
            case 700: return "Nghệ thuật";
            case 800: return "Văn học";
            case 900: return "Lịch sử & Địa lý";
            default: return "Khác";
        }
    }

    private String getShelfName(int shelf) {
        switch(shelf) {
            case 1: return "Kệ 1 - Tổng quan";
            case 2: return "Kệ 2 - Cơ bản";
            case 3: return "Kệ 3 - Nâng cao";
            case 4: return "Kệ 4 - Chuyên sâu";
            case 5: return "Kệ 5 - Tham khảo";
            default: return "Kệ " + shelf;
        }
    }

    private String getColumnName(int column) {
        switch(column) {
            case 1: return "Cột 1 - Lý thuyết";
            case 2: return "Cột 2 - Thực hành";
            case 3: return "Cột 3 - Bài tập";
            case 4: return "Cột 4 - Đề thi";
            case 5: return "Cột 5 - Tài liệu";
            default: return "Cột " + column;
        }
    }

    private String getRowName(int row) {
        switch(row) {
            case 1: return "Hàng 1 - Dễ";
            case 2: return "Hàng 2 - Trung bình";
            case 3: return "Hàng 3 - Khó";
            case 4: return "Hàng 4 - Chuyên gia";
            default: return "Hàng " + row;
        }
    }
}
