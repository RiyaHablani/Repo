package com.hashedin.huspark.service;

import com.hashedin.huspark.dto.BorrowRequest;
import com.hashedin.huspark.dto.BorrowingTransactionResponse;
import com.hashedin.huspark.entity.*;
import com.hashedin.huspark.exception.*;
import com.hashedin.huspark.repository.BookRepository;
import com.hashedin.huspark.repository.BorrowingTransactionRepository;
import com.hashedin.huspark.repository.UserRepository;
import com.hashedin.huspark.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowingServiceTest {

    @Mock
    private BorrowingTransactionRepository borrowingTransactionRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private AuditService auditService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private BorrowingService borrowingService;

    private User testUser;
    private Book testBook;
    private BorrowRequest borrowRequest;
    private BorrowingTransaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setRole(Role.MEMBER);
        testUser.setMaxBooksAllowed(5);

        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("1234567890123");
        testBook.setBarcode("BARCODE123");
        testBook.setAvailabilityStatus(BookStatus.AVAILABLE);
        testBook.setIsBorrowable(true);

        borrowRequest = new BorrowRequest();
        borrowRequest.setBookId(1L);

        testTransaction = new BorrowingTransaction();
        testTransaction.setId(1L);
        testTransaction.setUser(testUser);
        testTransaction.setBook(testBook);
        testTransaction.setBorrowedAt(LocalDateTime.now());
        testTransaction.setDueDate(LocalDateTime.now().plusDays(14));
        testTransaction.setStatus(TransactionStatus.BORROWED);
        testTransaction.setLateFee(BigDecimal.ZERO);
        testTransaction.setOverdue(false);
        testTransaction.setReminderSentCount(0);
    }

    @Test
    void testBorrowBook_WhenValidRequest_ShouldBorrowBook() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(borrowingTransactionRepository.findByUserIdAndBookIdAndStatusIn(
                eq(testUser.getId()), eq(testBook.getId()), any())).thenReturn(Optional.empty());
        when(borrowingTransactionRepository.countActiveBorrowingsByUserId(testUser.getId())).thenReturn(0L);
        when(borrowingTransactionRepository.save(any(BorrowingTransaction.class))).thenReturn(testTransaction);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            BorrowingTransactionResponse result = borrowingService.borrowBook(borrowRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getBookId()).isEqualTo(1L);
            assertThat(result.getBookTitle()).isEqualTo("Test Book");
            assertThat(result.getStatus()).isEqualTo(TransactionStatus.BORROWED);
            verify(bookRepository).findById(1L);
            verify(borrowingTransactionRepository).save(any(BorrowingTransaction.class));
            verify(bookRepository).save(any(Book.class));
            verify(auditService).logAction(eq("BOOK_BORROWED"), eq("BORROWING"), any(), any(), any());
        }
    }

    @Test
    void testBorrowBook_WhenBookNotFound_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            assertThatThrownBy(() -> borrowingService.borrowBook(borrowRequest))
                    .isInstanceOf(BookNotFoundException.class)
                    .hasMessage("Book not found with id: 1");
            verify(bookRepository).findById(1L);
            verify(borrowingTransactionRepository, never()).save(any(BorrowingTransaction.class));
        }
    }

    @Test
    void testBorrowBook_WhenBookNotBorrowable_ShouldThrowException() {
        // Given
        testBook.setIsBorrowable(false);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            assertThatThrownBy(() -> borrowingService.borrowBook(borrowRequest))
                    .isInstanceOf(BookNotBorrowableException.class)
                    .hasMessage("This book is not available for borrowing");
            verify(bookRepository).findById(1L);
            verify(borrowingTransactionRepository, never()).save(any(BorrowingTransaction.class));
        }
    }

    @Test
    void testBorrowBook_WhenBookNotAvailable_ShouldThrowException() {
        // Given
        testBook.setAvailabilityStatus(BookStatus.BORROWED);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            assertThatThrownBy(() -> borrowingService.borrowBook(borrowRequest))
                    .isInstanceOf(BookNotAvailableException.class)
                    .hasMessage("Book is not available for borrowing. Status: BORROWED");
            verify(bookRepository).findById(1L);
            verify(borrowingTransactionRepository, never()).save(any(BorrowingTransaction.class));
        }
    }

    @Test
    void testBorrowBook_WhenAlreadyBorrowed_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(borrowingTransactionRepository.findByUserIdAndBookIdAndStatusIn(
                eq(testUser.getId()), eq(testBook.getId()), any())).thenReturn(Optional.of(testTransaction));

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            assertThatThrownBy(() -> borrowingService.borrowBook(borrowRequest))
                    .isInstanceOf(BookAlreadyBorrowedException.class)
                    .hasMessage("You have already borrowed this book");
            verify(bookRepository).findById(1L);
            verify(borrowingTransactionRepository, never()).save(any(BorrowingTransaction.class));
        }
    }

    @Test
    void testBorrowBook_WhenBorrowingLimitExceeded_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(borrowingTransactionRepository.findByUserIdAndBookIdAndStatusIn(
                eq(testUser.getId()), eq(testBook.getId()), any())).thenReturn(Optional.empty());
        when(borrowingTransactionRepository.countActiveBorrowingsByUserId(testUser.getId())).thenReturn(5L);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            assertThatThrownBy(() -> borrowingService.borrowBook(borrowRequest))
                    .isInstanceOf(BorrowingLimitExceededException.class)
                    .hasMessage("Borrowing limit exceeded. You can borrow maximum 5 books.");
            verify(bookRepository).findById(1L);
            verify(borrowingTransactionRepository, never()).save(any(BorrowingTransaction.class));
        }
    }

    @Test
    void testReturnBook_WhenValidRequest_ShouldReturnBook() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(borrowingTransactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(borrowingTransactionRepository.save(any(BorrowingTransaction.class))).thenReturn(testTransaction);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            BorrowingTransactionResponse result = borrowingService.returnBook(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(TransactionStatus.RETURNED);
            verify(borrowingTransactionRepository).findById(1L);
            verify(borrowingTransactionRepository).save(any(BorrowingTransaction.class));
            verify(bookRepository).save(any(Book.class));
            verify(auditService).logAction(eq("BOOK_RETURNED"), eq("BORROWING"), any(), any(), any());
        }
    }

    @Test
    void testReturnBook_WhenTransactionNotFound_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(borrowingTransactionRepository.findById(1L)).thenReturn(Optional.empty());

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            assertThatThrownBy(() -> borrowingService.returnBook(1L))
                    .isInstanceOf(TransactionNotFoundException.class)
                    .hasMessage("Borrowing transaction not found with id: 1");
            verify(borrowingTransactionRepository).findById(1L);
            verify(borrowingTransactionRepository, never()).save(any(BorrowingTransaction.class));
        }
    }

    @Test
    void testReturnBook_WhenNotBorrowedByUser_ShouldThrowException() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        testTransaction.setUser(otherUser);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(borrowingTransactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            assertThatThrownBy(() -> borrowingService.returnBook(1L))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessage("You can only return books that you have borrowed");
            verify(borrowingTransactionRepository).findById(1L);
            verify(borrowingTransactionRepository, never()).save(any(BorrowingTransaction.class));
        }
    }

    @Test
    void testGetCurrentBorrowings_ShouldReturnUserBorrowings() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(borrowingTransactionRepository.findByUserIdAndStatusIn(eq(testUser.getId()), any())).thenReturn(Arrays.asList(testTransaction));

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            List<BorrowingTransactionResponse> result = borrowingService.getCurrentBorrowings();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getBookId()).isEqualTo(1L);
            assertThat(result.get(0).getBookTitle()).isEqualTo("Test Book");
            verify(borrowingTransactionRepository).findByUserIdAndStatusIn(eq(testUser.getId()), any());
        }
    }

    @Test
    void testGetAllOverdueTransactions_WhenLibrarian_ShouldReturnOverdueBooks() {
        // Given
        testUser.setRole(Role.LIBRARIAN);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(borrowingTransactionRepository.findOverdueTransactions(any(LocalDateTime.class))).thenReturn(Arrays.asList(testTransaction));

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            List<BorrowingTransactionResponse> result = borrowingService.getAllOverdueTransactions();

            // Then
            assertThat(result).hasSize(1);
            verify(borrowingTransactionRepository).findOverdueTransactions(any(LocalDateTime.class));
        }
    }

    @Test
    void testGetAllOverdueTransactions_WhenNotLibrarian_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            assertThatThrownBy(() -> borrowingService.getAllOverdueTransactions())
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessage("Only librarians can view overdue transactions");
            verify(borrowingTransactionRepository, never()).findOverdueTransactions(any(LocalDateTime.class));
        }
    }

    @Test
    void testSendReminder_WhenValidRequest_ShouldSendReminder() {
        // Given
        testUser.setRole(Role.LIBRARIAN);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(borrowingTransactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(borrowingTransactionRepository.save(any(BorrowingTransaction.class))).thenReturn(testTransaction);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            borrowingService.sendReminder(1L);

            // Then
            verify(borrowingTransactionRepository).findById(1L);
            verify(borrowingTransactionRepository).save(any(BorrowingTransaction.class));
            verify(notificationService).sendOverdueNotification(any(BorrowingTransaction.class));
            verify(auditService).logAction(eq("REMINDER_SENT"), eq("BORROWING"), any(), any(), any());
        }
    }

    @Test
    void testSendReminder_WhenMaxRemindersExceeded_ShouldThrowException() {
        // Given
        testUser.setRole(Role.LIBRARIAN);
        testTransaction.setReminderSentCount(3);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(borrowingTransactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            assertThatThrownBy(() -> borrowingService.sendReminder(1L))
                    .isInstanceOf(MaxRemindersExceededException.class)
                    .hasMessage("Maximum reminders already sent for this transaction");
            verify(borrowingTransactionRepository).findById(1L);
            verify(borrowingTransactionRepository, never()).save(any(BorrowingTransaction.class));
        }
    }
}
