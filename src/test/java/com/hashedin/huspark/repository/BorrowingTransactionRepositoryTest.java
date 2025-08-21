package com.hashedin.huspark.repository;

import com.hashedin.huspark.entity.Book;
import com.hashedin.huspark.entity.BookStatus;
import com.hashedin.huspark.entity.BorrowingTransaction;
import com.hashedin.huspark.entity.Role;
import com.hashedin.huspark.entity.TransactionStatus;
import com.hashedin.huspark.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BorrowingTransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BorrowingTransactionRepository borrowingTransactionRepository;

    private User testUser;
    private Book testBook;
    private BorrowingTransaction testTransaction;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("password123");
        testUser.setRole(Role.MEMBER);
        testUser.setMaxBooksAllowed(5);

        // Create test book
        testBook = new Book();
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("1234567890123");
        testBook.setBarcode("BARCODE123");
        testBook.setAvailabilityStatus(BookStatus.AVAILABLE);
        testBook.setIsBorrowable(true);

        // Create test transaction
        testTransaction = new BorrowingTransaction();
        testTransaction.setUser(testUser);
        testTransaction.setBook(testBook);
        testTransaction.setBorrowedAt(LocalDateTime.now());
        testTransaction.setDueDate(LocalDateTime.now().plusDays(14));
        testTransaction.setReturnedAt(null);
        testTransaction.setStatus(TransactionStatus.BORROWED);
        testTransaction.setLateFee(BigDecimal.ZERO);
        testTransaction.setOverdue(false);
        testTransaction.setReminderSentCount(0);
        testTransaction.setLastReminderSent(null);
        testTransaction.setNotes("Test borrowing");
    }

    @Test
    void testSaveTransaction_ShouldPersistTransaction() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        Book savedBook = entityManager.persistAndFlush(testBook);
        testTransaction.setUser(savedUser);
        testTransaction.setBook(savedBook);

        // When
        BorrowingTransaction savedTransaction = borrowingTransactionRepository.save(testTransaction);

        // Then
        assertThat(savedTransaction.getId()).isNotNull();
        assertThat(savedTransaction.getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(savedTransaction.getBook().getId()).isEqualTo(savedBook.getId());
        assertThat(savedTransaction.getStatus()).isEqualTo(TransactionStatus.BORROWED);
    }

    @Test
    void testFindById_WhenTransactionExists_ShouldReturnTransaction() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        Book savedBook = entityManager.persistAndFlush(testBook);
        testTransaction.setUser(savedUser);
        testTransaction.setBook(savedBook);
        BorrowingTransaction savedTransaction = entityManager.persistAndFlush(testTransaction);

        // When
        Optional<BorrowingTransaction> found = borrowingTransactionRepository.findById(savedTransaction.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(found.get().getBook().getId()).isEqualTo(savedBook.getId());
    }

    @Test
    void testFindById_WhenTransactionDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<BorrowingTransaction> found = borrowingTransactionRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void testFindByUserIdAndBookIdAndStatusIn_WhenTransactionExists_ShouldReturnTransaction() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        Book savedBook = entityManager.persistAndFlush(testBook);
        testTransaction.setUser(savedUser);
        testTransaction.setBook(savedBook);
        entityManager.persistAndFlush(testTransaction);

        List<TransactionStatus> activeStatuses = Arrays.asList(TransactionStatus.BORROWED, TransactionStatus.OVERDUE);

        // When
        Optional<BorrowingTransaction> found = borrowingTransactionRepository
                .findByUserIdAndBookIdAndStatusIn(savedUser.getId(), savedBook.getId(), activeStatuses);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(found.get().getBook().getId()).isEqualTo(savedBook.getId());
    }

    @Test
    void testFindByUserIdAndBookIdAndStatusIn_WhenTransactionDoesNotExist_ShouldReturnEmpty() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        Book savedBook = entityManager.persistAndFlush(testBook);
        List<TransactionStatus> activeStatuses = Arrays.asList(TransactionStatus.BORROWED, TransactionStatus.OVERDUE);

        // When
        Optional<BorrowingTransaction> found = borrowingTransactionRepository
                .findByUserIdAndBookIdAndStatusIn(savedUser.getId(), savedBook.getId(), activeStatuses);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void testFindByUserIdAndStatusIn_ShouldReturnTransactionsWithStatus() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        Book savedBook = entityManager.persistAndFlush(testBook);
        
        // Create borrowed transaction
        testTransaction.setUser(savedUser);
        testTransaction.setBook(savedBook);
        testTransaction.setStatus(TransactionStatus.BORROWED);
        entityManager.persistAndFlush(testTransaction);

        // Create returned transaction
        BorrowingTransaction returnedTransaction = new BorrowingTransaction();
        returnedTransaction.setUser(savedUser);
        returnedTransaction.setBook(savedBook);
        returnedTransaction.setBorrowedAt(LocalDateTime.now().minusDays(20));
        returnedTransaction.setDueDate(LocalDateTime.now().minusDays(6));
        returnedTransaction.setReturnedAt(LocalDateTime.now().minusDays(5));
        returnedTransaction.setStatus(TransactionStatus.RETURNED);
        returnedTransaction.setLateFee(BigDecimal.ZERO);
        returnedTransaction.setOverdue(false);
        entityManager.persistAndFlush(returnedTransaction);

        List<TransactionStatus> activeStatuses = Arrays.asList(TransactionStatus.BORROWED, TransactionStatus.OVERDUE);

        // When
        List<BorrowingTransaction> activeTransactions = borrowingTransactionRepository
                .findByUserIdAndStatusIn(savedUser.getId(), activeStatuses);

        // Then
        assertThat(activeTransactions).hasSize(1);
        assertThat(activeTransactions.get(0).getStatus()).isEqualTo(TransactionStatus.BORROWED);
    }

    @Test
    void testCountActiveBorrowingsByUserId_ShouldReturnCorrectCount() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        Book savedBook = entityManager.persistAndFlush(testBook);
        
        // Create borrowed transaction
        testTransaction.setUser(savedUser);
        testTransaction.setBook(savedBook);
        testTransaction.setStatus(TransactionStatus.BORROWED);
        entityManager.persistAndFlush(testTransaction);

        // Create overdue transaction
        BorrowingTransaction overdueTransaction = new BorrowingTransaction();
        overdueTransaction.setUser(savedUser);
        overdueTransaction.setBook(savedBook);
        overdueTransaction.setBorrowedAt(LocalDateTime.now().minusDays(20));
        overdueTransaction.setDueDate(LocalDateTime.now().minusDays(5));
        overdueTransaction.setReturnedAt(null);
        overdueTransaction.setStatus(TransactionStatus.OVERDUE);
        overdueTransaction.setLateFee(BigDecimal.valueOf(5.00));
        overdueTransaction.setOverdue(true);
        entityManager.persistAndFlush(overdueTransaction);

        // When
        Long activeCount = borrowingTransactionRepository.countActiveBorrowingsByUserId(savedUser.getId());

        // Then
        assertThat(activeCount).isEqualTo(2L);
    }

    @Test
    void testFindOverdueTransactions_ShouldReturnOverdueTransactions() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        Book savedBook = entityManager.persistAndFlush(testBook);
        
        // Create overdue transaction
        BorrowingTransaction overdueTransaction = new BorrowingTransaction();
        overdueTransaction.setUser(savedUser);
        overdueTransaction.setBook(savedBook);
        overdueTransaction.setBorrowedAt(LocalDateTime.now().minusDays(20));
        overdueTransaction.setDueDate(LocalDateTime.now().minusDays(5));
        overdueTransaction.setReturnedAt(null);
        overdueTransaction.setStatus(TransactionStatus.BORROWED);
        overdueTransaction.setLateFee(BigDecimal.valueOf(5.00));
        overdueTransaction.setOverdue(true);
        entityManager.persistAndFlush(overdueTransaction);

        // When
        List<BorrowingTransaction> overdueTransactions = borrowingTransactionRepository
                .findOverdueTransactions(LocalDateTime.now());

        // Then
        assertThat(overdueTransactions).hasSize(1);
        assertThat(overdueTransactions.get(0).getStatus()).isEqualTo(TransactionStatus.BORROWED);
        assertThat(overdueTransactions.get(0).getDueDate()).isBefore(LocalDateTime.now());
    }

    @Test
    void testFindByUserIdOrderByBorrowedAtDesc_ShouldReturnTransactionsInOrder() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        Book savedBook = entityManager.persistAndFlush(testBook);
        
        // Create first transaction
        BorrowingTransaction firstTransaction = new BorrowingTransaction();
        firstTransaction.setUser(savedUser);
        firstTransaction.setBook(savedBook);
        firstTransaction.setBorrowedAt(LocalDateTime.now().minusDays(10));
        firstTransaction.setDueDate(LocalDateTime.now().plusDays(4));
        firstTransaction.setStatus(TransactionStatus.BORROWED);
        firstTransaction.setLateFee(BigDecimal.ZERO);
        firstTransaction.setOverdue(false);
        entityManager.persistAndFlush(firstTransaction);

        // Create second transaction
        BorrowingTransaction secondTransaction = new BorrowingTransaction();
        secondTransaction.setUser(savedUser);
        secondTransaction.setBook(savedBook);
        secondTransaction.setBorrowedAt(LocalDateTime.now().minusDays(5));
        secondTransaction.setDueDate(LocalDateTime.now().plusDays(9));
        secondTransaction.setStatus(TransactionStatus.BORROWED);
        secondTransaction.setLateFee(BigDecimal.ZERO);
        secondTransaction.setOverdue(false);
        entityManager.persistAndFlush(secondTransaction);

        // When
        List<BorrowingTransaction> transactions = borrowingTransactionRepository
                .findByUserIdOrderByBorrowedAtDesc(savedUser.getId());

        // Then
        assertThat(transactions).hasSize(2);
        assertThat(transactions.get(0).getBorrowedAt()).isAfter(transactions.get(1).getBorrowedAt());
    }

    @Test
    void testFindByBookIdOrderByBorrowedAtDesc_ShouldReturnTransactionsInOrder() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        Book savedBook = entityManager.persistAndFlush(testBook);
        
        // Create first transaction
        BorrowingTransaction firstTransaction = new BorrowingTransaction();
        firstTransaction.setUser(savedUser);
        firstTransaction.setBook(savedBook);
        firstTransaction.setBorrowedAt(LocalDateTime.now().minusDays(10));
        firstTransaction.setDueDate(LocalDateTime.now().plusDays(4));
        firstTransaction.setStatus(TransactionStatus.RETURNED);
        firstTransaction.setReturnedAt(LocalDateTime.now().minusDays(5));
        firstTransaction.setLateFee(BigDecimal.ZERO);
        firstTransaction.setOverdue(false);
        entityManager.persistAndFlush(firstTransaction);

        // Create second transaction
        BorrowingTransaction secondTransaction = new BorrowingTransaction();
        secondTransaction.setUser(savedUser);
        secondTransaction.setBook(savedBook);
        secondTransaction.setBorrowedAt(LocalDateTime.now().minusDays(5));
        secondTransaction.setDueDate(LocalDateTime.now().plusDays(9));
        secondTransaction.setStatus(TransactionStatus.BORROWED);
        secondTransaction.setLateFee(BigDecimal.ZERO);
        secondTransaction.setOverdue(false);
        entityManager.persistAndFlush(secondTransaction);

        // When
        List<BorrowingTransaction> transactions = borrowingTransactionRepository
                .findByBookIdOrderByBorrowedAtDesc(savedBook.getId());

        // Then
        assertThat(transactions).hasSize(2);
        assertThat(transactions.get(0).getBorrowedAt()).isAfter(transactions.get(1).getBorrowedAt());
    }

    @Test
    void testCountByStatus_ShouldReturnCorrectCount() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        Book savedBook = entityManager.persistAndFlush(testBook);
        
        // Create borrowed transaction
        testTransaction.setUser(savedUser);
        testTransaction.setBook(savedBook);
        testTransaction.setStatus(TransactionStatus.BORROWED);
        entityManager.persistAndFlush(testTransaction);

        // Create another borrowed transaction
        BorrowingTransaction secondTransaction = new BorrowingTransaction();
        secondTransaction.setUser(savedUser);
        secondTransaction.setBook(savedBook);
        secondTransaction.setBorrowedAt(LocalDateTime.now().minusDays(5));
        secondTransaction.setDueDate(LocalDateTime.now().plusDays(9));
        secondTransaction.setStatus(TransactionStatus.BORROWED);
        secondTransaction.setLateFee(BigDecimal.ZERO);
        secondTransaction.setOverdue(false);
        entityManager.persistAndFlush(secondTransaction);

        // When
        Long borrowedCount = borrowingTransactionRepository.countByStatus(TransactionStatus.BORROWED);

        // Then
        assertThat(borrowedCount).isEqualTo(2L);
    }

    @Test
    void testDeleteTransaction_ShouldRemoveTransaction() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        Book savedBook = entityManager.persistAndFlush(testBook);
        testTransaction.setUser(savedUser);
        testTransaction.setBook(savedBook);
        BorrowingTransaction savedTransaction = entityManager.persistAndFlush(testTransaction);
        Long transactionId = savedTransaction.getId();

        // When
        borrowingTransactionRepository.deleteById(transactionId);

        // Then
        Optional<BorrowingTransaction> found = borrowingTransactionRepository.findById(transactionId);
        assertThat(found).isEmpty();
    }
}
