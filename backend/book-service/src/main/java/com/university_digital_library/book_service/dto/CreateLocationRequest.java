// book-service/src/main/java/.../dto/CreateLocationRequest.java
package com.university_digital_library.book_service.dto;

import lombok.Data;

@Data
public class CreateLocationRequest {
    private Long bookId;
    private Integer zone;
    private Integer shelf;
    private Integer column;
    private Integer row;
    private Integer position;
    private String branch;  // "B" hoặc "E"
}
