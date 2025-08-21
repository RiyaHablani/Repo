package com.hashedin.huspark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for reporting most borrowed books
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MostBorrowedBookReport {
    
    private Long bookId;
    private String title;
    private String author;
    private String isbn;
    private Long borrowCount;
    private LocalDateTime lastBorrowedDate;
    private String currentStatus;
    
    public MostBorrowedBookReport(Long bookId, String title, String author, String isbn, Long borrowCount) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.borrowCount = borrowCount;
    }
}
