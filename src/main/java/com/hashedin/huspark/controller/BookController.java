package com.hashedin.huspark.controller;

import com.hashedin.huspark.dto.BookRequest;
import com.hashedin.huspark.dto.BookResponse;
import com.hashedin.huspark.dto.PaginatedResponse;
import com.hashedin.huspark.entity.BookStatus;
import com.hashedin.huspark.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // MEMBERS can search and view books
    @GetMapping
    @PreAuthorize("hasRole('MEMBER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<BookResponse>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        PaginatedResponse<BookResponse> books = bookService.getAllBooksPaginated(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        BookResponse book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/barcode/{barcode}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<BookResponse> getBookByBarcode(@PathVariable String barcode) {
        BookResponse book = bookService.getBookByBarcode(barcode);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('MEMBER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<BookResponse>> searchBooks(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        PaginatedResponse<BookResponse> books = bookService.searchBooksPaginated(q, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<BookResponse>> getBooksByStatus(
            @PathVariable BookStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        PaginatedResponse<BookResponse> books = bookService.getBooksByStatusPaginated(status, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(books);
    }

    // LIBRARIANS can manage the book inventory
    @PostMapping
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest bookRequest) {
        BookResponse book = bookService.createBook(bookRequest);
        return new ResponseEntity<>(book, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<BookResponse> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest bookRequest) {
        BookResponse book = bookService.updateBook(id, bookRequest);
        return ResponseEntity.ok(book);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<BookResponse> updateBookStatus(@PathVariable Long id, @RequestParam BookStatus status) {
        BookResponse book = bookService.updateBookStatus(id, status);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('MEMBER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<BookResponse>> filterBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) BookStatus status,
            @RequestParam(required = false) String publisher,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        PaginatedResponse<BookResponse> books = bookService.getBooksWithFilters(
            title, author, genre, status, publisher, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(books);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
