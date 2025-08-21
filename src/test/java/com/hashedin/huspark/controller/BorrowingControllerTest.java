package com.hashedin.huspark.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.huspark.dto.BorrowRequest;
import com.hashedin.huspark.dto.BorrowingTransactionResponse;
import com.hashedin.huspark.entity.TransactionStatus;
import com.hashedin.huspark.exception.*;
import com.hashedin.huspark.service.BorrowingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BorrowingController.class)
class BorrowingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BorrowingService borrowingService;

    @Autowired
    private ObjectMapper objectMapper;

    private BorrowRequest borrowRequest;
    private BorrowingTransactionResponse transactionResponse;
    private List<BorrowingTransactionResponse> transactionResponses;

    @BeforeEach
    void setUp() {
        borrowRequest = new BorrowRequest();
        borrowRequest.setBookId(1L);
        borrowRequest.setDueDate(LocalDateTime.now().plusDays(14));

        transactionResponse = new BorrowingTransactionResponse();
        transactionResponse.setId(1L);
        transactionResponse.setUserId(1L);
        transactionResponse.setUserName("John Doe");
        transactionResponse.setUserEmail("john.doe@example.com");
        transactionResponse.setBookId(1L);
        transactionResponse.setBookTitle("Test Book");
        transactionResponse.setBookBarcode("BARCODE123");
        transactionResponse.setBorrowedAt(LocalDateTime.now());
        transactionResponse.setDueDate(LocalDateTime.now().plusDays(14));
        transactionResponse.setReturnedAt(null);
        transactionResponse.setStatus(TransactionStatus.BORROWED);
        transactionResponse.setLateFee(BigDecimal.ZERO);
        transactionResponse.setOverdue(false);
        transactionResponse.setReminderSentCount(0);
        transactionResponse.setLastReminderSent(null);
        transactionResponse.setNotes("Test borrowing");

        transactionResponses = Arrays.asList(transactionResponse);
    }

    @Test
    void testBorrowBook_WhenValidRequest_ShouldReturnCreated() throws Exception {
        // Given
        when(borrowingService.borrowBook(any(BorrowRequest.class))).thenReturn(transactionResponse);

        // When & Then
        mockMvc.perform(post("/api/borrowings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.bookTitle").value("Test Book"))
                .andExpect(jsonPath("$.status").value("BORROWED"));
    }

    @Test
    void testBorrowBook_WhenInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - Invalid request with missing required fields
        BorrowRequest invalidRequest = new BorrowRequest();
        // Missing bookId

        // When & Then
        mockMvc.perform(post("/api/borrowings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testBorrowBook_WhenBookNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        when(borrowingService.borrowBook(any(BorrowRequest.class)))
                .thenThrow(new BookNotFoundException("Book not found with id: 1"));

        // When & Then
        mockMvc.perform(post("/api/borrowings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Book not found with id: 1"));
    }

    @Test
    void testBorrowBook_WhenBookNotAvailable_ShouldReturnBadRequest() throws Exception {
        // Given
        when(borrowingService.borrowBook(any(BorrowRequest.class)))
                .thenThrow(new BookNotAvailableException("Book is not available for borrowing"));

        // When & Then
        mockMvc.perform(post("/api/borrowings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Book is not available for borrowing"));
    }

    @Test
    void testBorrowBook_WhenAlreadyBorrowed_ShouldReturnConflict() throws Exception {
        // Given
        when(borrowingService.borrowBook(any(BorrowRequest.class)))
                .thenThrow(new BookAlreadyBorrowedException("You have already borrowed this book"));

        // When & Then
        mockMvc.perform(post("/api/borrowings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().string("You have already borrowed this book"));
    }

    @Test
    void testBorrowBook_WhenBorrowingLimitExceeded_ShouldReturnBadRequest() throws Exception {
        // Given
        when(borrowingService.borrowBook(any(BorrowRequest.class)))
                .thenThrow(new BorrowingLimitExceededException("Borrowing limit exceeded"));

        // When & Then
        mockMvc.perform(post("/api/borrowings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Borrowing limit exceeded"));
    }

    @Test
    void testReturnBook_WhenValidRequest_ShouldReturnOk() throws Exception {
        // Given
        transactionResponse.setStatus(TransactionStatus.RETURNED);
        transactionResponse.setReturnedAt(LocalDateTime.now());
        when(borrowingService.returnBook(1L)).thenReturn(transactionResponse);

        // When & Then
        mockMvc.perform(put("/api/borrowings/1/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("RETURNED"));
    }

    @Test
    void testReturnBook_WhenTransactionNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        when(borrowingService.returnBook(1L))
                .thenThrow(new TransactionNotFoundException("Borrowing transaction not found with id: 1"));

        // When & Then
        mockMvc.perform(put("/api/borrowings/1/return"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Borrowing transaction not found with id: 1"));
    }

    @Test
    void testReturnBook_WhenNotBorrowedByUser_ShouldReturnForbidden() throws Exception {
        // Given
        when(borrowingService.returnBook(1L))
                .thenThrow(new UnauthorizedAccessException("You can only return books that you have borrowed"));

        // When & Then
        mockMvc.perform(put("/api/borrowings/1/return"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You can only return books that you have borrowed"));
    }

    @Test
    void testGetCurrentBorrowings_ShouldReturnUserBorrowings() throws Exception {
        // Given
        when(borrowingService.getCurrentBorrowings()).thenReturn(transactionResponses);

        // When & Then
        mockMvc.perform(get("/api/borrowings/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].bookTitle").value("Test Book"))
                .andExpect(jsonPath("$[0].status").value("BORROWED"));
    }

    @Test
    void testGetAllOverdueTransactions_WhenLibrarian_ShouldReturnOverdueBooks() throws Exception {
        // Given
        transactionResponse.setOverdue(true);
        when(borrowingService.getAllOverdueTransactions()).thenReturn(transactionResponses);

        // When & Then
        mockMvc.perform(get("/api/borrowings/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].overdue").value(true));
    }

    @Test
    void testGetAllOverdueTransactions_WhenNotLibrarian_ShouldReturnForbidden() throws Exception {
        // Given
        when(borrowingService.getAllOverdueTransactions())
                .thenThrow(new UnauthorizedAccessException("Only librarians can view overdue transactions"));

        // When & Then
        mockMvc.perform(get("/api/borrowings/overdue"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Only librarians can view overdue transactions"));
    }

    @Test
    void testSendReminder_WhenValidRequest_ShouldReturnOk() throws Exception {
        // Given
        doNothing().when(borrowingService).sendReminder(1L);

        // When & Then
        mockMvc.perform(post("/api/borrowings/1/reminder"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reminder sent successfully"));
    }

    @Test
    void testSendReminder_WhenTransactionNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        doThrow(new TransactionNotFoundException("Borrowing transaction not found with id: 1"))
                .when(borrowingService).sendReminder(1L);

        // When & Then
        mockMvc.perform(post("/api/borrowings/1/reminder"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Borrowing transaction not found with id: 1"));
    }

    @Test
    void testSendReminder_WhenMaxRemindersExceeded_ShouldReturnBadRequest() throws Exception {
        // Given
        doThrow(new MaxRemindersExceededException("Maximum reminders already sent for this transaction"))
                .when(borrowingService).sendReminder(1L);

        // When & Then
        mockMvc.perform(post("/api/borrowings/1/reminder"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Maximum reminders already sent for this transaction"));
    }

    @Test
    void testSendReminder_WhenNotLibrarian_ShouldReturnForbidden() throws Exception {
        // Given
        doThrow(new UnauthorizedAccessException("Only librarians can send reminders"))
                .when(borrowingService).sendReminder(1L);

        // When & Then
        mockMvc.perform(post("/api/borrowings/1/reminder"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Only librarians can send reminders"));
    }

    @Test
    void testGetUserBorrowingHistory_ShouldReturnHistory() throws Exception {
        // Given
        when(borrowingService.getUserBorrowingHistory()).thenReturn(transactionResponses);

        // When & Then
        mockMvc.perform(get("/api/borrowings/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].bookTitle").value("Test Book"));
    }

    @Test
    void testGetTransactionsDueForReminders_WhenLibrarian_ShouldReturnTransactions() throws Exception {
        // Given
        when(borrowingService.getTransactionsDueForReminders()).thenReturn(transactionResponses);

        // When & Then
        mockMvc.perform(get("/api/borrowings/due-for-reminders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void testGetTransactionsDueForReminders_WhenNotLibrarian_ShouldReturnForbidden() throws Exception {
        // Given
        when(borrowingService.getTransactionsDueForReminders())
                .thenThrow(new UnauthorizedAccessException("Only librarians can view reminder data"));

        // When & Then
        mockMvc.perform(get("/api/borrowings/due-for-reminders"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Only librarians can view reminder data"));
    }
}
