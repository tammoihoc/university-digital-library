// book-service/src/main/java/.../util/LocationGenerator.java
package com.university_digital_library.book_service.util;

import com.university_digital_library.book_service.constant.LocationConstants;
import com.university_digital_library.book_service.model.Book;
import com.university_digital_library.book_service.model.BookLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LocationGenerator {
    
    public BookLocation generateLocation(Book book) {
        int zone = determineZone(book);
        int shelf = determineShelf(book, zone);
        int column = determineColumn(book);
        int row = determineRow(book);
        int position = 1;
        
        String fullCode = String.format("%d%d%d.%d%d", zone / 100, shelf, column, row, position);
        
        String zoneName = getZoneName(zone);
        String shelfName = getShelfName(shelf);
        String columnName = getColumnName(column);
        String rowName = getRowName(row);
        
        BookLocation location = new BookLocation();
        location.setBook(book);
        location.setZone(zone);
        location.setShelf(shelf);
        location.setColNum(column);      
        location.setRowNum(row);         
        location.setPosition(position);
        location.setFullCode(fullCode);
        location.setIsAvailable(true);
        location.setZoneName(zoneName);
        location.setShelfName(shelfName);
        location.setColName(columnName);    // SỬA: setColumnName -> setColName
        location.setRowName(rowName);
        
        return location;
    }
    
    private int determineZone(Book book) {
        String categories = book.getCategories() != null ? String.join(",", book.getCategories()) : "";
        String department = book.getDepartment() != null ? book.getDepartment() : "";
        
        if (categories.contains("Ngôn ngữ") || department.contains("Ngôn ngữ")) {
            return 400;
        }
        if (categories.contains("Công nghệ") || categories.contains("Technology")) {
            return 600;
        }
        if (categories.contains("Văn học")) {
            return 800;
        }
        if (categories.contains("Khoa học")) {
            return 500;
        }
        if (categories.contains("Tâm lý")) {
            return 300;
        }
        return 400;
    }
    
    private int determineShelf(Book book, int zone) {
        String title = book.getTitle().toLowerCase();
        
        if (zone == 400) {
            if (title.contains("tiếng anh")) return 3;
            if (title.contains("tiếng việt")) return 2;
            if (title.contains("tiếng trung")) return 4;
            if (title.contains("tiếng nhật")) return 5;
            return 1;
        }
        return 1;
    }
    
    private int determineColumn(Book book) {
        String title = book.getTitle().toLowerCase();
        
        if (title.contains("ngữ pháp")) return 1;
        if (title.contains("từ vựng")) return 2;
        if (title.contains("phát âm")) return 3;
        if (title.contains("giao tiếp")) return 4;
        return 1;
    }
    
    private int determineRow(Book book) {
        String title = book.getTitle().toLowerCase();
        
        if (title.contains("cơ bản")) return 1;
        if (title.contains("nâng cao")) return 3;
        return 1;
    }
    
    private String getZoneName(int zone) {
        switch(zone) {
            case 300: return "Khoa học xã hội";
            case 400: return "Ngôn ngữ";
            case 500: return "Khoa học tự nhiên";
            case 600: return "Công nghệ";
            case 800: return "Văn học";
            default: return "Khác";
        }
    }
    
    private String getShelfName(int shelf) {
        switch(shelf) {
            case 1: return "Tổng quan";
            case 2: return "Tiếng Việt";
            case 3: return "Tiếng Anh";
            case 4: return "Tiếng Trung";
            case 5: return "Tiếng Nhật";
            default: return "Kệ " + shelf;
        }
    }
    
    private String getColumnName(int column) {
        switch(column) {
            case 1: return "Ngữ pháp";
            case 2: return "Từ vựng";
            case 3: return "Phát âm";
            case 4: return "Giao tiếp";
            default: return "Cột " + column;
        }
    }
    
    private String getRowName(int row) {
        switch(row) {
            case 1: return "Cơ bản";
            case 2: return "Trung cấp";
            case 3: return "Nâng cao";
            default: return "Hàng " + row;
        }
    }
}
