package com.hashedin.huspark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for reporting overdue items
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverdueItemReport {
    
    private Long transactionId;
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private Long userId;
    private String userName;
    private String userEmail;
    private LocalDateTime borrowedDate;
    private LocalDateTime dueDate;
    private LocalDateTime currentDate;
    private Long daysOverdue;
    private Double fineAmount;
    
    public OverdueItemReport(Long transactionId, Long bookId, String bookTitle, String bookIsbn, 
                           Long userId, String userName, String userEmail, 
                           LocalDateTime borrowedDate, LocalDateTime dueDate) {
        this.transactionId = transactionId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookIsbn = bookIsbn;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.borrowedDate = borrowedDate;
        this.dueDate = dueDate;
        this.currentDate = LocalDateTime.now();
        this.daysOverdue = calculateDaysOverdue();
        this.fineAmount = calculateFineAmount();
    }
    
    private Long calculateDaysOverdue() {
        if (dueDate == null || dueDate.isAfter(LocalDateTime.now())) {
            return 0L;
        }
        return java.time.Duration.between(dueDate, LocalDateTime.now()).toDays();
    }
    
    private Double calculateFineAmount() {
        // Simple fine calculation: $1 per day overdue
        return daysOverdue * 1.0;
    }
}
