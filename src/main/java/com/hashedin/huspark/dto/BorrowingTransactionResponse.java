package com.hashedin.huspark.dto;

import com.hashedin.huspark.entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingTransactionResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long bookId;
    private String bookTitle;
    private String bookBarcode;
    private LocalDateTime borrowedAt;
    private LocalDateTime dueDate;
    private LocalDateTime returnedAt;
    private TransactionStatus status;
    private BigDecimal lateFee;
    private boolean isOverdue;
    private Integer reminderSentCount;
    private LocalDateTime lastReminderSent;
    private String notes;
}
