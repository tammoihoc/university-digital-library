package com.university_digital_library.book_service.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Component
@Slf4j
public class PdfUtil {

    public Integer countPdfPagesFromPath(String filePath) {
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            log.error("Error reading PDF from path: {}", filePath, e);
            return null;
        }
    }

    public Integer countPdfPages(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try (InputStream is = file.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                return document.getNumberOfPages();
            }
        } catch (IOException e) {
            log.error("Error reading PDF from MultipartFile", e);
            return null;
        }
    }

    public long getFileSize(MultipartFile file) {
        return file.getSize();
    }

    public boolean isValidPdf(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        return (contentType != null && contentType.equals("application/pdf")) ||
               (filename != null && filename.toLowerCase().endsWith(".pdf"));
    }
}
