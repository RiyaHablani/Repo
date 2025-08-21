package com.hashedin.huspark.controller;

import com.hashedin.huspark.dto.BorrowRequest;
import com.hashedin.huspark.dto.BorrowingTransactionResponse;
import com.hashedin.huspark.service.BorrowingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrowing")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;

    // Member endpoints
    @PostMapping("/borrow")
    @PreAuthorize("hasRole('MEMBER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<BorrowingTransactionResponse> borrowBook(@Valid @RequestBody BorrowRequest request) {
        BorrowingTransactionResponse transaction = borrowingService.borrowBook(request);
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    @PostMapping("/return/{bookId}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<BorrowingTransactionResponse> returnBook(@PathVariable Long bookId) {
        BorrowingTransactionResponse transaction = borrowingService.returnBook(bookId);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('MEMBER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<BorrowingTransactionResponse>> getUserBorrowingHistory() {
        List<BorrowingTransactionResponse> history = borrowingService.getUserBorrowingHistory();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/current")
    @PreAuthorize("hasRole('MEMBER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<BorrowingTransactionResponse>> getCurrentBorrowings() {
        List<BorrowingTransactionResponse> currentBorrowings = borrowingService.getCurrentBorrowings();
        return ResponseEntity.ok(currentBorrowings);
    }

    // Librarian/Admin endpoints
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<BorrowingTransactionResponse>> getAllOverdueTransactions() {
        List<BorrowingTransactionResponse> overdueTransactions = borrowingService.getAllOverdueTransactions();
        return ResponseEntity.ok(overdueTransactions);
    }

    @GetMapping("/reminders")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<BorrowingTransactionResponse>> getTransactionsDueForReminders() {
        List<BorrowingTransactionResponse> reminderTransactions = borrowingService.getTransactionsDueForReminders();
        return ResponseEntity.ok(reminderTransactions);
    }

    @PostMapping("/reminders/{transactionId}/send")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Void> sendReminder(@PathVariable Long transactionId) {
        borrowingService.sendReminder(transactionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/late-fees/calculate")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Void> calculateLateFees() {
        borrowingService.calculateLateFees();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/inactive-users")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<Long>> getInactiveUserIds() {
        List<Long> inactiveUserIds = borrowingService.getInactiveUserIds();
        return ResponseEntity.ok(inactiveUserIds);
    }

    @GetMapping("/users/{userId}/contact-info")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<String> getUserContactInfo(@PathVariable Long userId) {
        String contactInfo = borrowingService.getEncryptedUserContactInfo(userId);
        return ResponseEntity.ok(contactInfo);
    }
}
