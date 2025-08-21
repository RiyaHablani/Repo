package com.hashedin.huspark.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrowing_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "borrowed_at", nullable = false)
    private LocalDateTime borrowedAt;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status = TransactionStatus.BORROWED;

    @Column(name = "late_fee", precision = 10, scale = 2)
    private BigDecimal lateFee = BigDecimal.ZERO;

    @Column(name = "is_overdue")
    private boolean isOverdue = false;

    @Column(name = "reminder_sent_count")
    private Integer reminderSentCount = 0;

    @Column(name = "last_reminder_sent")
    private LocalDateTime lastReminderSent;

    @Column(name = "notes")
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (borrowedAt == null) {
            borrowedAt = LocalDateTime.now();
        }
        if (dueDate == null) {
            // Default borrowing period: 14 days
            dueDate = borrowedAt.plusDays(14);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Check if overdue when transaction is updated
        if (status == TransactionStatus.BORROWED && dueDate.isBefore(LocalDateTime.now())) {
            isOverdue = true;
        }
    }
}
