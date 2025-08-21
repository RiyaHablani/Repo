package com.hashedin.huspark.service;

import com.hashedin.huspark.dto.MostBorrowedBookReport;
import com.hashedin.huspark.entity.Role;
import com.hashedin.huspark.entity.User;
import com.hashedin.huspark.repository.BookRepository;
import com.hashedin.huspark.repository.BorrowingTransactionRepository;
import com.hashedin.huspark.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BorrowingTransactionRepository borrowingTransactionRepository;

    @InjectMocks
    private ReportingService reportingService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setRole(Role.LIBRARIAN);
    }

    @Test
    void testGetMostBorrowedBooks_ShouldReturnReport() {
        // Given
        Object[] bookData = {1L, "Test Book", "Test Author", "1234567890123", 5L};
        when(borrowingTransactionRepository.findMostBorrowedBooks(anyInt())).thenReturn(Arrays.asList(bookData));

        // When
        List<MostBorrowedBookReport> result = reportingService.getMostBorrowedBooks(10);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBookId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Book");
        assertThat(result.get(0).getBorrowCount()).isEqualTo(5L);
        verify(borrowingTransactionRepository).findMostBorrowedBooks(10);
    }

    @Test
    void testGetMostBorrowedBooks_WhenExceptionOccurs_ShouldHandleGracefully() {
        // Given
        when(borrowingTransactionRepository.findMostBorrowedBooks(anyInt()))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> reportingService.getMostBorrowedBooks(10))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to generate most borrowed books report");
    }
}
