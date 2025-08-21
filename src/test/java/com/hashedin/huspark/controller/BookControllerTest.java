package com.hashedin.huspark.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.huspark.dto.BookRequest;
import com.hashedin.huspark.dto.BookResponse;
import com.hashedin.huspark.dto.PaginatedResponse;
import com.hashedin.huspark.entity.BookStatus;
import com.hashedin.huspark.exception.BookAlreadyExistsException;
import com.hashedin.huspark.exception.BookNotFoundException;
import com.hashedin.huspark.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    private BookRequest bookRequest;
    private BookResponse bookResponse;
    private List<BookResponse> bookResponses;

    @BeforeEach
    void setUp() {
        bookRequest = new BookRequest();
        bookRequest.setTitle("Test Book");
        bookRequest.setAuthor("Test Author");
        bookRequest.setIsbn("1234567890123");
        bookRequest.setBarcode("BARCODE123");
        bookRequest.setDescription("Test description");
        bookRequest.setPublicationYear(2023);
        bookRequest.setPublisher("Test Publisher");
        bookRequest.setGenre("Fiction");

        bookResponse = new BookResponse();
        bookResponse.setId(1L);
        bookResponse.setTitle("Test Book");
        bookResponse.setAuthor("Test Author");
        bookResponse.setIsbn("1234567890123");
        bookResponse.setBarcode("BARCODE123");
        bookResponse.setAvailabilityStatus(BookStatus.AVAILABLE);
        bookResponse.setDescription("Test description");
        bookResponse.setPublicationYear(2023);
        bookResponse.setPublisher("Test Publisher");
        bookResponse.setGenre("Fiction");
        bookResponse.setCreatedAt(LocalDateTime.now());
        bookResponse.setUpdatedAt(LocalDateTime.now());

        bookResponses = Arrays.asList(bookResponse);
    }

    @Test
    void testGetAllBooks_ShouldReturnAllBooks() throws Exception {
        // Given
        when(bookService.getAllBooks()).thenReturn(bookResponses);

        // When & Then
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Book"))
                .andExpect(jsonPath("$[0].author").value("Test Author"));
    }

    @Test
    void testGetAllBooksPaginated_ShouldReturnPaginatedBooks() throws Exception {
        // Given
        PaginatedResponse<BookResponse> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setContent(bookResponses);
        paginatedResponse.setTotalElements(1L);
        paginatedResponse.setTotalPages(1);
        paginatedResponse.setPageNumber(0);
        paginatedResponse.setPageSize(10);
        
        when(bookService.getAllBooksPaginated(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(paginatedResponse);

        // When & Then
        mockMvc.perform(get("/api/books/paginated")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "title")
                        .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    void testGetBookById_WhenBookExists_ShouldReturnBook() throws Exception {
        // Given
        when(bookService.getBookById(1L)).thenReturn(bookResponse);

        // When & Then
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.author").value("Test Author"));
    }

    @Test
    void testGetBookById_WhenBookDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(bookService.getBookById(1L))
                .thenThrow(new BookNotFoundException("Book not found with id: 1"));

        // When & Then
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Book not found with id: 1"));
    }

    @Test
    void testGetBookByBarcode_WhenBookExists_ShouldReturnBook() throws Exception {
        // Given
        when(bookService.getBookByBarcode("BARCODE123")).thenReturn(bookResponse);

        // When & Then
        mockMvc.perform(get("/api/books/barcode/BARCODE123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.barcode").value("BARCODE123"))
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    void testGetBookByBarcode_WhenBookDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(bookService.getBookByBarcode("INVALID"))
                .thenThrow(new BookNotFoundException("Book not found with barcode: INVALID"));

        // When & Then
        mockMvc.perform(get("/api/books/barcode/INVALID"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Book not found with barcode: INVALID"));
    }

    @Test
    void testSearchBooks_ShouldReturnMatchingBooks() throws Exception {
        // Given
        when(bookService.searchBooks("Test")).thenReturn(bookResponses);

        // When & Then
        mockMvc.perform(get("/api/books/search")
                        .param("q", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Book"));
    }

    @Test
    void testGetBooksByStatus_ShouldReturnBooksWithStatus() throws Exception {
        // Given
        when(bookService.getBooksByStatus(BookStatus.AVAILABLE)).thenReturn(bookResponses);

        // When & Then
        mockMvc.perform(get("/api/books/status/AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].availabilityStatus").value("AVAILABLE"));
    }

    @Test
    void testCreateBook_WhenValidRequest_ShouldReturnCreated() throws Exception {
        // Given
        when(bookService.createBook(any(BookRequest.class))).thenReturn(bookResponse);

        // When & Then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.author").value("Test Author"));
    }

    @Test
    void testCreateBook_WhenInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - Invalid request with missing required fields
        BookRequest invalidRequest = new BookRequest();
        invalidRequest.setTitle(""); // Empty title

        // When & Then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateBook_WhenBookAlreadyExists_ShouldReturnConflict() throws Exception {
        // Given
        when(bookService.createBook(any(BookRequest.class)))
                .thenThrow(new BookAlreadyExistsException("Book already exists with ISBN: 1234567890123"));

        // When & Then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Book already exists with ISBN: 1234567890123"));
    }

    @Test
    void testUpdateBook_WhenValidRequest_ShouldReturnOk() throws Exception {
        // Given
        when(bookService.updateBook(eq(1L), any(BookRequest.class))).thenReturn(bookResponse);

        // When & Then
        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    void testUpdateBook_WhenBookNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        when(bookService.updateBook(eq(1L), any(BookRequest.class)))
                .thenThrow(new BookNotFoundException("Book not found with id: 1"));

        // When & Then
        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Book not found with id: 1"));
    }

    @Test
    void testDeleteBook_WhenBookExists_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(bookService).deleteBook(1L);

        // When & Then
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteBook_WhenBookNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        doThrow(new BookNotFoundException("Book not found with id: 1"))
                .when(bookService).deleteBook(1L);

        // When & Then
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Book not found with id: 1"));
    }
}
