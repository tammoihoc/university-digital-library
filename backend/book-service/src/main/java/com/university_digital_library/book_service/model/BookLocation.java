package com.university_digital_library.book_service.model;

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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    private Integer copyNumber;
    private Integer zone;
    private Integer shelf;
    
    @Column(name = "col_num")
    private Integer colNum;
    
    @Column(name = "row_num")
    private Integer rowNum;
    
    private Integer position;
    
    @Column(unique = true, nullable = false)
    private String fullCode;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;
    
    private String zoneName;
    private String shelfName;
    
    @Column(name = "col_name")
    private String colName;
    
    @Column(name = "row_name")
    private String rowName;
    
    public String getFullLocation() {
        return String.format("Bản %d - Khu %d (%s) - Kệ %d (%s) - Cột %d (%s) - Hàng %d (%s) - Vị trí %d",
            copyNumber, zone, zoneName, shelf, shelfName, colNum, colName, rowNum, rowName, position);
    }
}
