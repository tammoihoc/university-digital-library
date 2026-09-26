package com.university_digital_library.book_service.dto;

import com.university_digital_library.book_service.model.Book;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateBookRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Author is required")
    private String author;
    
    private String isbn;
    private String description;
    
    @NotNull(message = "Book type is required")
    private Book.BookType type;
    
    @NotNull(message = "Access type is required")
    private Book.BookAccessType accessType;
    
    private String publisher;
    private Integer publicationYear;
    private Integer pages;
    private String language;
    private String department;
    private String courseCode;
    private List<String> categories;
    private Double price;
    private Integer totalPhysicalCopies;
    private Long libraryBranchId;
}
