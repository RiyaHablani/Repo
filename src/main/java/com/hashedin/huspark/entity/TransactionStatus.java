package com.hashedin.huspark.entity;

public enum TransactionStatus {
    BORROWED,    // Book is currently borrowed
    RETURNED,    // Book has been returned
    OVERDUE,     // Book is overdue (past due date)
    LOST         // Book is reported as lost
}
