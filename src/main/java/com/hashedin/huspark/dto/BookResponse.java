package com.hashedin.huspark.dto;

import com.hashedin.huspark.entity.BookStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String barcode;
    private BookStatus availabilityStatus;
    private String description;
    private Integer publicationYear;
    private String publisher;
    private String genre;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
