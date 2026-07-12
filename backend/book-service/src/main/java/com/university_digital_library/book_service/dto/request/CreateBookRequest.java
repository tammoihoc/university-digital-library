package com.university_digital_library.book_service.dto.request;

import com.university_digital_library.book_service.model.BookAccessType;
import com.university_digital_library.book_service.model.BookType;
import lombok.Data;
import java.util.List;

@Data
public class CreateBookRequest {
    private String title;
    private String author;
    private String isbn;
    private String description;
    private BookType type;
    private BookAccessType accessType;
    private String publisher;
    private Integer publicationYear;
    private Integer pages;
    private Integer totalPhysicalCopies;
    private String department;
    private String courseCode;
    private List<String> categories;
    private Double price;
    // Thêm các getter/setter nếu Lombok không hoạt động
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BookType getType() { return type; }
    public void setType(BookType type) { this.type = type; }
    
    public BookAccessType getAccessType() { return accessType; }
    public void setAccessType(BookAccessType accessType) { this.accessType = accessType; }
    
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    
    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }
    
    public Integer getPages() { return pages; }
    public void setPages(Integer pages) { this.pages = pages; }
    
    public Integer getTotalPhysicalCopies() { return totalPhysicalCopies; }
    public void setTotalPhysicalCopies(Integer totalPhysicalCopies) { this.totalPhysicalCopies = totalPhysicalCopies; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
}
