package com.hashedin.huspark.config;

import com.hashedin.huspark.entity.Book;
import com.hashedin.huspark.entity.BookStatus;
import com.hashedin.huspark.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final BookRepository bookRepository;

    @Bean
    @Profile("local")
    CommandLineRunner initDatabase() {
        return args -> {
            log.info("Loading initial book data...");
            
            // Only load data if no books exist
            if (bookRepository.count() == 0) {
                loadSampleBooks();
                log.info("Sample books loaded successfully!");
            } else {
                log.info("Books already exist, skipping data loading.");
            }
        };
    }

    private void loadSampleBooks() {
        Book book1 = new Book();
        book1.setTitle("The Hobbit");
        book1.setAuthor("J.R.R. Tolkien");
        book1.setIsbn("978-0547928241");
        book1.setBarcode("SAMPLE001");
        book1.setDescription("A fantasy novel about Bilbo Baggins, a hobbit who embarks on a journey with thirteen dwarves.");
        book1.setPublicationYear(1937);
        book1.setPublisher("Houghton Mifflin Harcourt");
        book1.setGenre("Fantasy");
        book1.setAvailabilityStatus(BookStatus.AVAILABLE);
        bookRepository.save(book1);

        Book book2 = new Book();
        book2.setTitle("1984");
        book2.setAuthor("George Orwell");
        book2.setIsbn("978-0451524935");
        book2.setBarcode("SAMPLE002");
        book2.setDescription("A dystopian novel about totalitarianism and surveillance society.");
        book2.setPublicationYear(1949);
        book2.setPublisher("Signet Classic");
        book2.setGenre("Dystopian Fiction");
        book2.setAvailabilityStatus(BookStatus.AVAILABLE);
        bookRepository.save(book2);

        Book book3 = new Book();
        book3.setTitle("Pride and Prejudice");
        book3.setAuthor("Jane Austen");
        book3.setIsbn("978-0141439518");
        book3.setBarcode("SAMPLE003");
        book3.setDescription("A romantic novel of manners about the relationship between Elizabeth Bennet and Mr. Darcy.");
        book3.setPublicationYear(1813);
        book3.setPublisher("Penguin Classics");
        book3.setGenre("Romance");
        book3.setAvailabilityStatus(BookStatus.BORROWED);
        bookRepository.save(book3);

        Book book4 = new Book();
        book4.setTitle("The Catcher in the Rye");
        book4.setAuthor("J.D. Salinger");
        book4.setIsbn("978-0316769488");
        book4.setBarcode("SAMPLE004");
        book4.setDescription("A novel about teenage alienation and loss of innocence in post-World War II America.");
        book4.setPublicationYear(1951);
        book4.setPublisher("Little, Brown and Company");
        book4.setGenre("Coming-of-age");
        book4.setAvailabilityStatus(BookStatus.AVAILABLE);
        bookRepository.save(book4);

        Book book5 = new Book();
        book5.setTitle("The Lord of the Rings");
        book5.setAuthor("J.R.R. Tolkien");
        book5.setIsbn("978-0547928210");
        book5.setBarcode("SAMPLE005");
        book5.setDescription("An epic high-fantasy novel about the quest to destroy a powerful ring.");
        book5.setPublicationYear(1954);
        book5.setPublisher("Houghton Mifflin Harcourt");
        book5.setGenre("Fantasy");
        book5.setAvailabilityStatus(BookStatus.MAINTENANCE);
        bookRepository.save(book5);
    }
}
