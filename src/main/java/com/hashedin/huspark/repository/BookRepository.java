package com.hashedin.huspark.repository;

import com.hashedin.huspark.entity.Book;
import com.hashedin.huspark.entity.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByBarcode(String barcode);
    
    Optional<Book> findByIsbn(String isbn);
    
    List<Book> findByTitleContainingIgnoreCase(String title);
    
    List<Book> findByAuthorContainingIgnoreCase(String author);
    
    List<Book> findByAvailabilityStatus(BookStatus status);
    
    Page<Book> findByAvailabilityStatus(BookStatus status, Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.barcode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.genre) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Book> searchBooks(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.barcode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.genre) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Book> searchBooksPaginated(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE " +
           "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
           "(:genre IS NULL OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%'))) AND " +
           "(:status IS NULL OR b.availabilityStatus = :status) AND " +
           "(:publisher IS NULL OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :publisher, '%')))")
    Page<Book> findBooksWithFilters(
            @Param("title") String title,
            @Param("author") String author,
            @Param("genre") String genre,
            @Param("status") BookStatus status,
            @Param("publisher") String publisher,
            Pageable pageable);
    
    boolean existsByBarcode(String barcode);
    
    boolean existsByIsbn(String isbn);
}
