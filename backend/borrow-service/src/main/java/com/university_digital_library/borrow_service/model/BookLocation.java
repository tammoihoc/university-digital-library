// borrow-service/src/main/java/.../model/BookLocation.java
package com.university_digital_library.borrow_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book_locations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookLocation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "book_id", nullable = false)
    private Long bookId;
    
    private Integer copyNumber;
    private Integer zone;
    private Integer shelf;
    private Integer column;
    private Integer row;
    private Integer position;
    private String fullCode;
    private Boolean isAvailable;
    private String zoneName;
    private String shelfName;
    private String columnName;
    private String rowName;
}
