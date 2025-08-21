package com.hashedin.huspark.service;

import com.hashedin.huspark.dto.BorrowRequest;
import com.hashedin.huspark.dto.BorrowingTransactionResponse;
import com.hashedin.huspark.entity.*;
import com.hashedin.huspark.exception.*;
import com.hashedin.huspark.repository.BookRepository;
import com.hashedin.huspark.repository.BorrowingTransactionRepository;
import com.hashedin.huspark.repository.UserRepository;
import com.hashedin.huspark.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowingService {

    private final BorrowingTransactionRepository borrowingTransactionRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final AuditService auditService;
    private final NotificationService notificationService;

    // Default borrowing period in days
    private static final int DEFAULT_BORROWING_DAYS = 14;
    private static final BigDecimal DAILY_LATE_FEE = new BigDecimal("1.00"); // $1 per day
    private static final int MAX_REMINDERS = 3;

    // Helper method to get current user
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    // Helper method to check if user has required role
    private boolean hasRole(Role requiredRole) {
        User currentUser = getCurrentUser();
        return currentUser.getRole().ordinal() >= requiredRole.ordinal();
    }

    // Convert BorrowingTransaction to DTO
    private BorrowingTransactionResponse convertToResponse(BorrowingTransaction transaction) {
        return new BorrowingTransactionResponse(
            transaction.getId(),
            transaction.getUser().getId(),
            transaction.getUser().getName(),
            transaction.getUser().getEmail(),
            transaction.getBook().getId(),
            transaction.getBook().getTitle(),
            transaction.getBook().getBarcode(),
            transaction.getBorrowedAt(),
            transaction.getDueDate(),
            transaction.getReturnedAt(),
            transaction.getStatus(),
            transaction.getLateFee(),
            transaction.isOverdue(),
            transaction.getReminderSentCount(),
            transaction.getLastReminderSent(),
            transaction.getNotes()
        );
    }

    @Transactional
    public BorrowingTransactionResponse borrowBook(BorrowRequest request) {
        User currentUser = getCurrentUser();
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + request.getBookId()));

        // Check if book is borrowable
        if (!book.getIsBorrowable()) {
            throw new BookNotBorrowableException("This book is not available for borrowing");
        }

        // Check if book is available
        if (book.getAvailabilityStatus() != BookStatus.AVAILABLE) {
            throw new BookNotAvailableException("Book is not available for borrowing. Status: " + book.getAvailabilityStatus());
        }

        // Check if user has already borrowed this book
        List<TransactionStatus> activeStatuses = List.of(TransactionStatus.BORROWED, TransactionStatus.OVERDUE);
        if (borrowingTransactionRepository.findByUserIdAndBookIdAndStatusIn(
                currentUser.getId(), book.getId(), activeStatuses).isPresent()) {
            throw new BookAlreadyBorrowedException("You have already borrowed this book");
        }

        // Check user's borrowing limit
        Long activeBorrowings = borrowingTransactionRepository.countActiveBorrowingsByUserId(currentUser.getId());
        if (activeBorrowings >= currentUser.getMaxBooksAllowed()) {
            throw new BorrowingLimitExceededException("You have reached your maximum borrowing limit of " + currentUser.getMaxBooksAllowed() + " books");
        }

        // Create borrowing transaction
        BorrowingTransaction transaction = new BorrowingTransaction();
        transaction.setUser(currentUser);
        transaction.setBook(book);
        transaction.setBorrowedAt(LocalDateTime.now());
        
        // Set due date
        LocalDateTime dueDate = request.getDueDate();
        if (dueDate == null) {
            dueDate = LocalDateTime.now().plusDays(book.getMaxBorrowingDays());
        }
        transaction.setDueDate(dueDate);
        transaction.setStatus(TransactionStatus.BORROWED);

        // Update book status
        book.setAvailabilityStatus(BookStatus.BORROWED);
        bookRepository.save(book);

        // Update user statistics
        currentUser.setLastBorrowingDate(LocalDateTime.now());
        currentUser.setTotalBooksBorrowed(currentUser.getTotalBooksBorrowed() + 1);
        userRepository.save(currentUser);

        BorrowingTransaction savedTransaction = borrowingTransactionRepository.save(transaction);
        
        // Log the book borrowing
        auditService.logBookBorrow(book.getId(), currentUser.getId(), savedTransaction.getId());
        
        // Send notification
        notificationService.sendBookBorrowedNotification(savedTransaction);
        
        log.info("Book '{}' borrowed by user '{}'", book.getTitle(), currentUser.getEmail());
        
        return convertToResponse(savedTransaction);
    }

    @Transactional
    public BorrowingTransactionResponse returnBook(Long bookId) {
        User currentUser = getCurrentUser();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));

        // Find active borrowing transaction
        List<TransactionStatus> activeStatuses = List.of(TransactionStatus.BORROWED, TransactionStatus.OVERDUE);
        BorrowingTransaction transaction = borrowingTransactionRepository
                .findByUserIdAndBookIdAndStatusIn(currentUser.getId(), bookId, activeStatuses)
                .orElseThrow(() -> new BookNotBorrowedException("You have not borrowed this book"));

        // Calculate late fee if overdue
        BigDecimal lateFee = BigDecimal.ZERO;
        if (transaction.getDueDate().isBefore(LocalDateTime.now())) {
            long overdueDays = ChronoUnit.DAYS.between(transaction.getDueDate(), LocalDateTime.now());
            lateFee = DAILY_LATE_FEE.multiply(BigDecimal.valueOf(overdueDays));
            transaction.setLateFee(lateFee);
            transaction.setOverdue(true);
            
            // Update user's overdue count
            currentUser.setOverdueCount(currentUser.getOverdueCount() + 1);
            userRepository.save(currentUser);
        }

        // Update transaction
        transaction.setReturnedAt(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.RETURNED);

        // Update book status
        book.setAvailabilityStatus(BookStatus.AVAILABLE);
        bookRepository.save(book);

        BorrowingTransaction savedTransaction = borrowingTransactionRepository.save(transaction);
        
        // Log the book return
        auditService.logBookReturn(book.getId(), currentUser.getId(), savedTransaction.getId());
        
        // Send notification
        notificationService.sendBookReturnedNotification(savedTransaction);
        
        log.info("Book '{}' returned by user '{}' with late fee: ${}", book.getTitle(), currentUser.getEmail(), lateFee);
        
        return convertToResponse(savedTransaction);
    }

    public List<BorrowingTransactionResponse> getUserBorrowingHistory() {
        User currentUser = getCurrentUser();
        List<BorrowingTransaction> transactions = borrowingTransactionRepository
                .findByUserIdOrderByBorrowedAtDesc(currentUser.getId());
        return transactions.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public List<BorrowingTransactionResponse> getCurrentBorrowings() {
        User currentUser = getCurrentUser();
        List<TransactionStatus> activeStatuses = List.of(TransactionStatus.BORROWED, TransactionStatus.OVERDUE);
        List<BorrowingTransaction> transactions = borrowingTransactionRepository
                .findByUserIdAndStatusIn(currentUser.getId(), activeStatuses);
        return transactions.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    // Admin/Librarian methods
    public List<BorrowingTransactionResponse> getAllOverdueTransactions() {
        if (!hasRole(Role.LIBRARIAN)) {
            throw new UnauthorizedAccessException("Only librarians can view overdue transactions");
        }

        List<BorrowingTransaction> overdueTransactions = borrowingTransactionRepository
                .findOverdueTransactions(LocalDateTime.now());
        return overdueTransactions.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public List<BorrowingTransactionResponse> getTransactionsDueForReminders() {
        if (!hasRole(Role.LIBRARIAN)) {
            throw new UnauthorizedAccessException("Only librarians can view reminder data");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.plusDays(1); // Due tomorrow
        LocalDateTime endDate = now.plusDays(3);   // Due in 3 days
        LocalDateTime reminderThreshold = now.minusDays(1); // Last reminder sent more than 1 day ago

        List<BorrowingTransaction> transactions = borrowingTransactionRepository
                .findTransactionsDueForReminders(startDate, endDate, reminderThreshold);
        return transactions.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public List<Long> getInactiveUserIds() {
        if (!hasRole(Role.LIBRARIAN)) {
            throw new UnauthorizedAccessException("Only librarians can view inactive user data");
        }

        LocalDateTime thresholdDate = LocalDateTime.now().minusMonths(6); // 6 months
        return borrowingTransactionRepository.findInactiveUserIds(thresholdDate);
    }

    @Transactional
    public void sendReminder(Long transactionId) {
        if (!hasRole(Role.LIBRARIAN)) {
            throw new UnauthorizedAccessException("Only librarians can send reminders");
        }

        BorrowingTransaction transaction = borrowingTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

        if (transaction.getReminderSentCount() >= MAX_REMINDERS) {
            throw new MaxRemindersExceededException("Maximum reminders already sent for this transaction");
        }

        transaction.setReminderSentCount(transaction.getReminderSentCount() + 1);
        transaction.setLastReminderSent(LocalDateTime.now());
        borrowingTransactionRepository.save(transaction);

        log.info("Reminder sent for transaction {} to user {}", transactionId, transaction.getUser().getEmail());
    }

    @Transactional
    public void calculateLateFees() {
        if (!hasRole(Role.LIBRARIAN)) {
            throw new UnauthorizedAccessException("Only librarians can calculate late fees");
        }

        List<BorrowingTransaction> transactions = borrowingTransactionRepository
                .findTransactionsNeedingLateFeeCalculation();

        for (BorrowingTransaction transaction : transactions) {
            if (transaction.getReturnedAt().isAfter(transaction.getDueDate())) {
                long overdueDays = ChronoUnit.DAYS.between(transaction.getDueDate(), transaction.getReturnedAt());
                BigDecimal lateFee = DAILY_LATE_FEE.multiply(BigDecimal.valueOf(overdueDays));
                transaction.setLateFee(lateFee);
                borrowingTransactionRepository.save(transaction);
            }
        }
    }

    // Get encrypted user contact information (for authorized staff only)
    public String getEncryptedUserContactInfo(Long userId) {
        if (!hasRole(Role.LIBRARIAN)) {
            throw new UnauthorizedAccessException("Only librarians can access user contact information");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Return encrypted contact information
        String contactInfo = String.format("Phone: %s, Address: %s", 
            user.getPhone() != null ? user.getPhone() : "N/A",
            user.getAddress() != null ? user.getAddress() : "N/A");
        
        return contactInfo;
    }
}
