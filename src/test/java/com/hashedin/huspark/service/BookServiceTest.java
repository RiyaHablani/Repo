package com.hashedin.huspark.service;

import com.hashedin.huspark.dto.BookRequest;
import com.hashedin.huspark.dto.BookResponse;
import com.hashedin.huspark.dto.PaginatedResponse;
import com.hashedin.huspark.entity.Book;
import com.hashedin.huspark.entity.BookStatus;
import com.hashedin.huspark.entity.Role;
import com.hashedin.huspark.entity.User;
import com.hashedin.huspark.exception.BookAlreadyExistsException;
import com.hashedin.huspark.exception.BookNotFoundException;
import com.hashedin.huspark.exception.UnauthorizedAccessException;
import com.hashedin.huspark.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private BookService bookService;

    private User testUser;
    private Book testBook;
    private BookRequest bookRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setRole(Role.LIBRARIAN);

        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("1234567890123");
        testBook.setBarcode("BARCODE123");
        testBook.setAvailabilityStatus(BookStatus.AVAILABLE);
        testBook.setDescription("Test description");
        testBook.setPublicationYear(2023);
        testBook.setPublisher("Test Publisher");
        testBook.setGenre("Fiction");
        testBook.setCreatedAt(LocalDateTime.now());
        testBook.setUpdatedAt(LocalDateTime.now());

        bookRequest = new BookRequest();
        bookRequest.setTitle("New Book");
        bookRequest.setAuthor("New Author");
        bookRequest.setIsbn("9876543210987");
        bookRequest.setBarcode("NEWBARCODE");
        bookRequest.setDescription("New description");
        bookRequest.setPublicationYear(2024);
        bookRequest.setPublisher("New Publisher");
        bookRequest.setGenre("Non-Fiction");
    }

    @Test
    void testGetAllBooks_ShouldReturnAllBooks() {
        // Given
        List<Book> books = Arrays.asList(testBook);
        when(bookRepository.findAll()).thenReturn(books);

        // When
        List<BookResponse> result = bookService.getAllBooks();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Book");
        assertThat(result.get(0).getAuthor()).isEqualTo("Test Author");
        verify(bookRepository).findAll();
    }

    @Test
    void testGetAllBooksPaginated_ShouldReturnPaginatedBooks() {
        // Given
        Page<Book> bookPage = new PageImpl<>(Arrays.asList(testBook));
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(bookPage);

        // When
        PaginatedResponse<BookResponse> result = bookService.getAllBooksPaginated(0, 10, "title", "asc");

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        verify(bookRepository).findAll(any(Pageable.class));
    }

    @Test
    void testGetBookById_WhenBookExists_ShouldReturnBook() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // When
        BookResponse result = bookService.getBookById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Book");
        assertThat(result.getAuthor()).isEqualTo("Test Author");
        verify(bookRepository).findById(1L);
    }

    @Test
    void testGetBookById_WhenBookDoesNotExist_ShouldThrowException() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.getBookById(1L))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("Book not found with id: 1");
        verify(bookRepository).findById(1L);
    }

    @Test
    void testGetBookByBarcode_WhenBookExists_ShouldReturnBook() {
        // Given
        when(bookRepository.findByBarcode("BARCODE123")).thenReturn(Optional.of(testBook));

        // When
        BookResponse result = bookService.getBookByBarcode("BARCODE123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBarcode()).isEqualTo("BARCODE123");
        verify(bookRepository).findByBarcode("BARCODE123");
    }

    @Test
    void testGetBookByBarcode_WhenBookDoesNotExist_ShouldThrowException() {
        // Given
        when(bookRepository.findByBarcode("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.getBookByBarcode("INVALID"))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("Book not found with barcode: INVALID");
        verify(bookRepository).findByBarcode("INVALID");
    }

    @Test
    void testSearchBooks_ShouldReturnMatchingBooks() {
        // Given
        List<Book> books = Arrays.asList(testBook);
        when(bookRepository.searchBooks("Test")).thenReturn(books);

        // When
        List<BookResponse> result = bookService.searchBooks("Test");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Book");
        verify(bookRepository).searchBooks("Test");
    }

    @Test
    void testGetBooksByStatus_ShouldReturnBooksWithStatus() {
        // Given
        List<Book> books = Arrays.asList(testBook);
        when(bookRepository.findByAvailabilityStatus(BookStatus.AVAILABLE)).thenReturn(books);

        // When
        List<BookResponse> result = bookService.getBooksByStatus(BookStatus.AVAILABLE);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAvailabilityStatus()).isEqualTo(BookStatus.AVAILABLE);
        verify(bookRepository).findByAvailabilityStatus(BookStatus.AVAILABLE);
    }

    @Test
    void testCreateBook_WhenLibrarian_ShouldCreateBook() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookRepository.existsByIsbn(bookRequest.getIsbn())).thenReturn(false);
        when(bookRepository.existsByBarcode(bookRequest.getBarcode())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            BookResponse result = bookService.createBook(bookRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Test Book");
            verify(bookRepository).existsByIsbn(bookRequest.getIsbn());
            verify(bookRepository).existsByBarcode(bookRequest.getBarcode());
            verify(bookRepository).save(any(Book.class));
            verify(auditService).logBookCreation(eq(testBook.getId()), eq(testBook.getTitle()));
        }
    }

    @Test
    void testCreateBook_WhenNotLibrarian_ShouldThrowException() {
        // Given
        testUser.setRole(Role.MEMBER);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            assertThatThrownBy(() -> bookService.createBook(bookRequest))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessage("Access denied. Librarian role required.");
            verify(bookRepository, never()).save(any(Book.class));
        }
    }

    @Test
    void testCreateBook_WhenIsbnExists_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookRepository.existsByIsbn(bookRequest.getIsbn())).thenReturn(true);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            assertThatThrownBy(() -> bookService.createBook(bookRequest))
                    .isInstanceOf(BookAlreadyExistsException.class)
                    .hasMessage("Book already exists with ISBN: " + bookRequest.getIsbn());
            verify(bookRepository).existsByIsbn(bookRequest.getIsbn());
            verify(bookRepository, never()).save(any(Book.class));
        }
    }

    @Test
    void testCreateBook_WhenBarcodeExists_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookRepository.existsByIsbn(bookRequest.getIsbn())).thenReturn(false);
        when(bookRepository.existsByBarcode(bookRequest.getBarcode())).thenReturn(true);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            assertThatThrownBy(() -> bookService.createBook(bookRequest))
                    .isInstanceOf(BookAlreadyExistsException.class)
                    .hasMessage("Book already exists with barcode: " + bookRequest.getBarcode());
            verify(bookRepository).existsByIsbn(bookRequest.getIsbn());
            verify(bookRepository).existsByBarcode(bookRequest.getBarcode());
            verify(bookRepository, never()).save(any(Book.class));
        }
    }

    @Test
    void testUpdateBook_WhenLibrarian_ShouldUpdateBook() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            BookResponse result = bookService.updateBook(1L, bookRequest);

            // Then
            assertThat(result).isNotNull();
            verify(bookRepository).findById(1L);
            verify(bookRepository).save(any(Book.class));
            verify(auditService).logBookUpdate(eq(testBook.getId()), eq(testBook.getTitle()), any(Map.class));
        }
    }

    @Test
    void testUpdateBook_WhenBookNotFound_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            assertThatThrownBy(() -> bookService.updateBook(1L, bookRequest))
                    .isInstanceOf(BookNotFoundException.class)
                    .hasMessage("Book not found with id: 1");
            verify(bookRepository).findById(1L);
            verify(bookRepository, never()).save(any(Book.class));
        }
    }

    @Test
    void testDeleteBook_WhenLibrarian_ShouldDeleteBook() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            bookService.deleteBook(1L);

            // Then
            verify(bookRepository).findById(1L);
            verify(bookRepository).delete(testBook);
            verify(auditService).logBookDeletion(eq(testBook.getId()), eq(testBook.getTitle()));
        }
    }

    @Test
    void testDeleteBook_WhenBookNotFound_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            assertThatThrownBy(() -> bookService.deleteBook(1L))
                    .isInstanceOf(BookNotFoundException.class)
                    .hasMessage("Book not found with id: 1");
            verify(bookRepository).findById(1L);
            verify(bookRepository, never()).delete(any(Book.class));
        }
    }
}
