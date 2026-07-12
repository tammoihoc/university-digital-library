// book-service/src/main/java/.../model/LibraryBranch.java
package com.university_digital_library.book_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "library_branches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryBranch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, length = 500)
    private String address;
    
    @Column(length = 500)
    private String openingHours;
    
    private String phone;
    
    private String email;
    
    private String mapUrl;
    
    @Builder.Default
    private Boolean isActive = true;
}
