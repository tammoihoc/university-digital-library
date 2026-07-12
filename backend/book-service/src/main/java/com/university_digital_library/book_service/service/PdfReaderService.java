package com.university_digital_library.book_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PdfReaderService {
    
    private final Path fileStorageLocation = Paths.get("uploads/pdf").toAbsolutePath().normalize();
    
    public PdfReaderService() {
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }
    
    public Map<String, Object> uploadPdfFile(Long bookId, MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }
            
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null) {
                throw new RuntimeException("File name is null");
            }
            
            // Check if it's a PDF file
            if (!originalFileName.toLowerCase().endsWith(".pdf")) {
                throw new RuntimeException("Only PDF files are allowed");
            }
            
            // Generate unique filename
            String newFileName = "book_" + bookId + "_" + System.currentTimeMillis() + ".pdf";
            Path targetLocation = fileStorageLocation.resolve(newFileName);
            
            // Copy file to target location
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Get file size
            long fileSize = Files.size(targetLocation);
            
            Map<String, Object> response = new HashMap<>();
            response.put("fileName", newFileName);
            response.put("fileUrl", "/api/books/pdf/read/" + newFileName);
            response.put("fileSize", fileSize);
            response.put("pageCount", estimatePageCount(fileSize));
            response.put("message", "PDF uploaded successfully");
            
            log.info("PDF uploaded for book {}: {}", bookId, newFileName);
            
            return response;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload PDF: " + e.getMessage());
        }
    }
    
    private int estimatePageCount(long fileSize) {
        // Rough estimation: ~50KB per page
        int estimatedPages = (int) (fileSize / 50000);
        return Math.max(1, estimatedPages);
    }
}
