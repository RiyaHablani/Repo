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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuditService auditService;

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

    // Convert Book entity to BookResponse DTO
    private BookResponse convertToResponse(Book book) {
        return new BookResponse(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getIsbn(),
            book.getBarcode(),
            book.getAvailabilityStatus(),
            book.getDescription(),
            book.getPublicationYear(),
            book.getPublisher(),
            book.getGenre(),
            book.getCreatedAt(),
            book.getUpdatedAt()
        );
    }

    // MEMBERS can search and view books
    public List<BookResponse> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        return books.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public PaginatedResponse<BookResponse> getAllBooksPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Book> bookPage = bookRepository.findAll(pageable);
        
        return convertToPaginatedResponse(bookPage);
    }

    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
        return convertToResponse(book);
    }

    public BookResponse getBookByBarcode(String barcode) {
        Book book = bookRepository.findByBarcode(barcode)
                .orElseThrow(() -> new BookNotFoundException("Book not found with barcode: " + barcode));
        return convertToResponse(book);
    }

    public List<BookResponse> searchBooks(String searchTerm) {
        List<Book> books = bookRepository.searchBooks(searchTerm);
        return books.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<BookResponse> getBooksByStatus(BookStatus status) {
        List<Book> books = bookRepository.findByAvailabilityStatus(status);
        return books.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // LIBRARIANS can manage the book inventory
    public BookResponse createBook(BookRequest bookRequest) {
        if (!hasRole(Role.LIBRARIAN)) {
            throw new UnauthorizedAccessException("Only librarians can create books");
        }

        // Check for duplicate barcode
        if (bookRepository.existsByBarcode(bookRequest.getBarcode())) {
            throw new BookAlreadyExistsException("Book with barcode " + bookRequest.getBarcode() + " already exists");
        }

        // Check for duplicate ISBN if provided
        if (bookRequest.getIsbn() != null && !bookRequest.getIsbn().trim().isEmpty() 
            && bookRepository.existsByIsbn(bookRequest.getIsbn())) {
            throw new BookAlreadyExistsException("Book with ISBN " + bookRequest.getIsbn() + " already exists");
        }

        Book book = new Book();
        book.setTitle(bookRequest.getTitle());
        book.setAuthor(bookRequest.getAuthor());
        book.setIsbn(bookRequest.getIsbn());
        book.setBarcode(bookRequest.getBarcode());
        book.setDescription(bookRequest.getDescription());
        book.setPublicationYear(bookRequest.getPublicationYear());
        book.setPublisher(bookRequest.getPublisher());
        book.setGenre(bookRequest.getGenre());
        book.setAvailabilityStatus(BookStatus.AVAILABLE);

        Book savedBook = bookRepository.save(book);
        
        // Log the book creation
        auditService.logBookCreation(savedBook.getId(), savedBook.getTitle());
        
        return convertToResponse(savedBook);
    }

    public BookResponse updateBook(Long id, BookRequest bookRequest) {
        if (!hasRole(Role.LIBRARIAN)) {
            throw new UnauthorizedAccessException("Only librarians can update books");
        }

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));

        // Check for duplicate barcode if changed
        if (!book.getBarcode().equals(bookRequest.getBarcode()) 
            && bookRepository.existsByBarcode(bookRequest.getBarcode())) {
            throw new BookAlreadyExistsException("Book with barcode " + bookRequest.getBarcode() + " already exists");
        }

        // Check for duplicate ISBN if changed
        if (bookRequest.getIsbn() != null && !bookRequest.getIsbn().trim().isEmpty() 
            && !bookRequest.getIsbn().equals(book.getIsbn()) 
            && bookRepository.existsByIsbn(bookRequest.getIsbn())) {
            throw new BookAlreadyExistsException("Book with ISBN " + bookRequest.getIsbn() + " already exists");
        }

        book.setTitle(bookRequest.getTitle());
        book.setAuthor(bookRequest.getAuthor());
        book.setIsbn(bookRequest.getIsbn());
        book.setBarcode(bookRequest.getBarcode());
        book.setDescription(bookRequest.getDescription());
        book.setPublicationYear(bookRequest.getPublicationYear());
        book.setPublisher(bookRequest.getPublisher());
        book.setGenre(bookRequest.getGenre());

        // Track changes for audit log
        Map<String, Object> changes = Map.of(
            "oldTitle", book.getTitle(),
            "newTitle", bookRequest.getTitle(),
            "oldAuthor", book.getAuthor(),
            "newAuthor", bookRequest.getAuthor()
        );

        Book updatedBook = bookRepository.save(book);
        
        // Log the book update
        auditService.logBookUpdate(updatedBook.getId(), updatedBook.getTitle(), changes);
        
        return convertToResponse(updatedBook);
    }

    public BookResponse updateBookStatus(Long id, BookStatus status) {
        if (!hasRole(Role.LIBRARIAN)) {
            throw new UnauthorizedAccessException("Only librarians can update book status");
        }

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));

        book.setAvailabilityStatus(status);
        Book updatedBook = bookRepository.save(book);
        return convertToResponse(updatedBook);
    }

    public void deleteBook(Long id) {
        if (!hasRole(Role.LIBRARIAN)) {
            throw new UnauthorizedAccessException("Only librarians can delete books");
        }

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));

        String bookTitle = book.getTitle();
        bookRepository.deleteById(id);
        
        // Log the book deletion
        auditService.logBookDeletion(id, bookTitle);
    }

    // ADMIN has full access (inherits all LIBRARIAN permissions)
    // Additional admin-specific methods can be added here if needed

    public PaginatedResponse<BookResponse> searchBooksPaginated(String searchTerm, int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Book> bookPage = bookRepository.searchBooksPaginated(searchTerm, pageable);
        
        return convertToPaginatedResponse(bookPage);
    }

    public PaginatedResponse<BookResponse> getBooksByStatusPaginated(BookStatus status, int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Book> bookPage = bookRepository.findByAvailabilityStatus(status, pageable);
        
        return convertToPaginatedResponse(bookPage);
    }

    public PaginatedResponse<BookResponse> getBooksWithFilters(String title, String author, String genre, 
                                                             BookStatus status, String publisher, 
                                                             int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Book> bookPage = bookRepository.findBooksWithFilters(title, author, genre, status, publisher, pageable);
        
        return convertToPaginatedResponse(bookPage);
    }

    private PaginatedResponse<BookResponse> convertToPaginatedResponse(Page<Book> bookPage) {
        List<BookResponse> content = bookPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new PaginatedResponse<>(
            content,
            bookPage.getNumber(),
            bookPage.getSize(),
            bookPage.getTotalElements(),
            bookPage.getTotalPages(),
            bookPage.hasNext(),
            bookPage.hasPrevious(),
            bookPage.isFirst(),
            bookPage.isLast()
        );
    }
}
