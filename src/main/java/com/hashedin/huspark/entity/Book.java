package com.hashedin.huspark.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank
    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "isbn", unique = true)
    private String isbn;

    @NotBlank
    @Column(name = "barcode", nullable = false, unique = true)
    private String barcode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false)
    private BookStatus availabilityStatus = BookStatus.AVAILABLE;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "genre")
    private String genre;

    @Column(name = "max_borrowing_days")
    private Integer maxBorrowingDays = 14; // Default borrowing period

    @Column(name = "is_borrowable")
    private Boolean isBorrowable = true; // Default to borrowable

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
