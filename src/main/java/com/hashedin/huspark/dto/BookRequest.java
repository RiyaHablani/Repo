package com.hashedin.huspark.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    private String isbn;

    @NotBlank(message = "Barcode is required")
    private String barcode;

    private String description;
    private Integer publicationYear;
    private String publisher;
    private String genre;
}
