package com.hashedin.huspark.repository;

import com.hashedin.huspark.entity.Book;
import com.hashedin.huspark.entity.BookStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("1234567890123");
        testBook.setBarcode("BARCODE123");
        testBook.setAvailabilityStatus(BookStatus.AVAILABLE);
        testBook.setDescription("Test description");
        testBook.setPublicationYear(2023);
        testBook.setPublisher("Test Publisher");
        testBook.setGenre("Fiction");
        testBook.setIsBorrowable(true);
        testBook.setCreatedAt(LocalDateTime.now());
        testBook.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testSaveBook_ShouldPersistBook() {
        // When
        Book savedBook = bookRepository.save(testBook);

        // Then
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getTitle()).isEqualTo("Test Book");
        assertThat(savedBook.getAuthor()).isEqualTo("Test Author");
        assertThat(savedBook.getIsbn()).isEqualTo("1234567890123");
        assertThat(savedBook.getBarcode()).isEqualTo("BARCODE123");
        assertThat(savedBook.getAvailabilityStatus()).isEqualTo(BookStatus.AVAILABLE);
    }

    @Test
    void testFindById_WhenBookExists_ShouldReturnBook() {
        // Given
        Book savedBook = entityManager.persistAndFlush(testBook);

        // When
        Optional<Book> found = bookRepository.findById(savedBook.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Book");
        assertThat(found.get().getAuthor()).isEqualTo("Test Author");
    }

    @Test
    void testFindById_WhenBookDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<Book> found = bookRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void testFindByBarcode_WhenBookExists_ShouldReturnBook() {
        // Given
        entityManager.persistAndFlush(testBook);

        // When
        Optional<Book> found = bookRepository.findByBarcode("BARCODE123");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getBarcode()).isEqualTo("BARCODE123");
        assertThat(found.get().getTitle()).isEqualTo("Test Book");
    }

    @Test
    void testFindByBarcode_WhenBookDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<Book> found = bookRepository.findByBarcode("INVALID");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void testExistsByIsbn_WhenBookExists_ShouldReturnTrue() {
        // Given
        entityManager.persistAndFlush(testBook);

        // When
        boolean exists = bookRepository.existsByIsbn("1234567890123");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByIsbn_WhenBookDoesNotExist_ShouldReturnFalse() {
        // When
        boolean exists = bookRepository.existsByIsbn("9876543210987");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void testExistsByBarcode_WhenBookExists_ShouldReturnTrue() {
        // Given
        entityManager.persistAndFlush(testBook);

        // When
        boolean exists = bookRepository.existsByBarcode("BARCODE123");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByBarcode_WhenBookDoesNotExist_ShouldReturnFalse() {
        // When
        boolean exists = bookRepository.existsByBarcode("INVALID");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void testFindByAvailabilityStatus_ShouldReturnBooksWithStatus() {
        // Given
        Book book1 = new Book();
        book1.setTitle("Available Book");
        book1.setAuthor("Author 1");
        book1.setIsbn("1111111111111");
        book1.setBarcode("BARCODE111");
        book1.setAvailabilityStatus(BookStatus.AVAILABLE);
        book1.setIsBorrowable(true);

        Book book2 = new Book();
        book2.setTitle("Borrowed Book");
        book2.setAuthor("Author 2");
        book2.setIsbn("2222222222222");
        book2.setBarcode("BARCODE222");
        book2.setAvailabilityStatus(BookStatus.BORROWED);
        book2.setIsBorrowable(true);

        entityManager.persistAndFlush(book1);
        entityManager.persistAndFlush(book2);

        // When
        List<Book> availableBooks = bookRepository.findByAvailabilityStatus(BookStatus.AVAILABLE);

        // Then
        assertThat(availableBooks).hasSize(1);
        assertThat(availableBooks.get(0).getTitle()).isEqualTo("Available Book");
        assertThat(availableBooks.get(0).getAvailabilityStatus()).isEqualTo(BookStatus.AVAILABLE);
    }

    @Test
    void testSearchBooks_ShouldReturnMatchingBooks() {
        // Given
        Book book1 = new Book();
        book1.setTitle("Java Programming");
        book1.setAuthor("John Doe");
        book1.setIsbn("1111111111111");
        book1.setBarcode("BARCODE111");
        book1.setAvailabilityStatus(BookStatus.AVAILABLE);
        book1.setIsBorrowable(true);

        Book book2 = new Book();
        book2.setTitle("Python Programming");
        book2.setAuthor("Jane Smith");
        book2.setIsbn("2222222222222");
        book2.setBarcode("BARCODE222");
        book2.setAvailabilityStatus(BookStatus.AVAILABLE);
        book2.setIsBorrowable(true);

        Book book3 = new Book();
        book3.setTitle("JavaScript Basics");
        book3.setAuthor("Bob Johnson");
        book3.setIsbn("3333333333333");
        book3.setBarcode("BARCODE333");
        book3.setAvailabilityStatus(BookStatus.AVAILABLE);
        book3.setIsBorrowable(true);

        entityManager.persistAndFlush(book1);
        entityManager.persistAndFlush(book2);
        entityManager.persistAndFlush(book3);

        // When
        List<Book> programmingBooks = bookRepository.searchBooks("Programming");

        // Then
        assertThat(programmingBooks).hasSize(2);
        assertThat(programmingBooks).extracting("title")
                .containsExactlyInAnyOrder("Java Programming", "Python Programming");
    }

    @Test
    void testSearchBooks_WhenNoMatch_ShouldReturnEmpty() {
        // Given
        entityManager.persistAndFlush(testBook);

        // When
        List<Book> results = bookRepository.searchBooks("NonExistent");

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void testFindAll_ShouldReturnAllBooks() {
        // Given
        Book book1 = new Book();
        book1.setTitle("Book 1");
        book1.setAuthor("Author 1");
        book1.setIsbn("1111111111111");
        book1.setBarcode("BARCODE111");
        book1.setAvailabilityStatus(BookStatus.AVAILABLE);
        book1.setIsBorrowable(true);

        Book book2 = new Book();
        book2.setTitle("Book 2");
        book2.setAuthor("Author 2");
        book2.setIsbn("2222222222222");
        book2.setBarcode("BARCODE222");
        book2.setAvailabilityStatus(BookStatus.AVAILABLE);
        book2.setIsBorrowable(true);

        entityManager.persistAndFlush(book1);
        entityManager.persistAndFlush(book2);

        // When
        List<Book> allBooks = bookRepository.findAll();

        // Then
        assertThat(allBooks).hasSize(2);
        assertThat(allBooks).extracting("title")
                .containsExactlyInAnyOrder("Book 1", "Book 2");
    }

    @Test
    void testDeleteBook_ShouldRemoveBook() {
        // Given
        Book savedBook = entityManager.persistAndFlush(testBook);
        Long bookId = savedBook.getId();

        // When
        bookRepository.deleteById(bookId);

        // Then
        Optional<Book> found = bookRepository.findById(bookId);
        assertThat(found).isEmpty();
    }

    @Test
    void testUpdateBook_ShouldUpdateBook() {
        // Given
        Book savedBook = entityManager.persistAndFlush(testBook);
        savedBook.setTitle("Updated Title");
        savedBook.setDescription("Updated description");

        // When
        Book updatedBook = bookRepository.save(savedBook);

        // Then
        assertThat(updatedBook.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedBook.getDescription()).isEqualTo("Updated description");
        
        // Verify in database
        Book found = entityManager.find(Book.class, savedBook.getId());
        assertThat(found.getTitle()).isEqualTo("Updated Title");
    }
}
