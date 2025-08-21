# Book Catalog Implementation with Role-Based Access Control (RBAC)

## Overview
Successfully implemented a comprehensive book catalog system with strict Role-Based Access Control (RBAC) for the HuSpark library management system.

## 🏗️ Architecture

### Database Schema
The book catalog uses a robust database schema with the following key components:

#### Book Entity (`Book.java`)
- **Primary Key**: `id` (Auto-generated)
- **Required Fields**: `title`, `author`, `barcode`
- **Optional Fields**: `isbn`, `description`, `publicationYear`, `publisher`, `genre`
- **Status Management**: `availabilityStatus` (enum: AVAILABLE, BORROWED, RESERVED, MAINTENANCE, LOST)
- **Audit Fields**: `createdAt`, `updatedAt` (auto-managed)

#### BookStatus Enum (`BookStatus.java`)
```java
public enum BookStatus {
    AVAILABLE,    // Book is available for borrowing
    BORROWED,     // Book is currently borrowed
    RESERVED,     // Book is reserved by a member
    MAINTENANCE,  // Book is under maintenance
    LOST          // Book is lost
}
```

## 🔐 Role-Based Access Control (RBAC)

### Role Hierarchy
```
ADMIN > LIBRARIAN > MEMBER
```

### Permission Matrix

| Operation | MEMBER | LIBRARIAN | ADMIN |
|-----------|--------|-----------|-------|
| View all books | ✅ | ✅ | ✅ |
| Search books | ✅ | ✅ | ✅ |
| Get book by ID | ✅ | ✅ | ✅ |
| Get book by barcode | ✅ | ✅ | ✅ |
| Filter by status | ✅ | ✅ | ✅ |
| Create books | ❌ | ✅ | ✅ |
| Update books | ❌ | ✅ | ✅ |
| Update book status | ❌ | ✅ | ✅ |
| Delete books | ❌ | ✅ | ✅ |

## 📚 API Endpoints

### Public Endpoints
None - All book operations require authentication

### Member Access (MEMBER, LIBRARIAN, ADMIN)
```
GET /api/books                    - Get all books
GET /api/books/{id}               - Get book by ID
GET /api/books/barcode/{barcode}  - Get book by barcode
GET /api/books/search?q={term}    - Search books
GET /api/books/status/{status}    - Get books by status
```

### Librarian Access (LIBRARIAN, ADMIN)
```
POST /api/books                   - Create new book
PUT /api/books/{id}               - Update book
PATCH /api/books/{id}/status      - Update book status
DELETE /api/books/{id}            - Delete book
```

## 🔍 Search Functionality

### Multi-field Search
The search endpoint searches across multiple fields:
- Title (case-insensitive)
- Author (case-insensitive)
- ISBN (case-insensitive)
- Barcode (case-insensitive)
- Genre (case-insensitive)

### Barcode Quick Search
- Optimized for quick book lookup during borrowing/returning operations
- Unique constraint ensures no duplicate barcodes
- Fast database indexing for quick retrieval

## 🛡️ Security Features

### Authentication
- JWT-based authentication required for all endpoints
- Token validation on every request

### Authorization
- `@PreAuthorize` annotations enforce role-based access
- Service-layer validation for additional security
- Hierarchical role system (ADMIN inherits all permissions)

### Data Validation
- Input validation using `@Valid` annotations
- Custom validation for required fields
- Duplicate barcode/ISBN prevention
- Proper error handling with meaningful messages

## 📊 Data Transfer Objects (DTOs)

### BookRequest
```java
public class BookRequest {
    @NotBlank private String title;
    @NotBlank private String author;
    private String isbn;
    @NotBlank private String barcode;
    private String description;
    private Integer publicationYear;
    private String publisher;
    private String genre;
}
```

### BookResponse
```java
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String barcode;
    private BookStatus availabilityStatus;
    private String description;
    private Integer publicationYear;
    private String publisher;
    private String genre;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

## 🗄️ Repository Layer

### BookRepository
- Extends `JpaRepository` for basic CRUD operations
- Custom query methods for specific searches
- Optimized search query with multiple field support
- Existence checks for duplicate prevention

### Key Methods
```java
Optional<Book> findByBarcode(String barcode);
Optional<Book> findByIsbn(String isbn);
List<Book> searchBooks(@Param("searchTerm") String searchTerm);
boolean existsByBarcode(String barcode);
boolean existsByIsbn(String isbn);
```

## 🎯 Service Layer

### BookService
- Business logic implementation
- RBAC enforcement at service level
- Data validation and transformation
- Exception handling

### Key Features
- Role-based access control validation
- Duplicate prevention (barcode, ISBN)
- Entity to DTO conversion
- Comprehensive error handling

## 🚨 Exception Handling

### Custom Exceptions
- `BookNotFoundException` - When book is not found
- `BookAlreadyExistsException` - When duplicate barcode/ISBN
- `UnauthorizedAccessException` - When insufficient permissions

### Global Exception Handler
- Centralized error handling
- Proper HTTP status codes
- Meaningful error messages

## 📈 Testing Results

### ✅ Successfully Tested Features
1. **Book Search**: Multi-field search working correctly
2. **Barcode Lookup**: Fast barcode-based book retrieval
3. **Role-based Access**: Proper permission enforcement
4. **CRUD Operations**: All create, read, update, delete operations
5. **Status Management**: Book availability status updates
6. **Data Validation**: Input validation and duplicate prevention
7. **Error Handling**: Proper error responses for edge cases

### Sample Data
The system includes 5 sample books for testing:
1. The Hobbit (J.R.R. Tolkien) - AVAILABLE
2. 1984 (George Orwell) - AVAILABLE
3. Pride and Prejudice (Jane Austen) - BORROWED
4. The Catcher in the Rye (J.D. Salinger) - AVAILABLE
5. The Lord of the Rings (J.R.R. Tolkien) - MAINTENANCE

## 🔧 Configuration

### Data Loading
- Automatic sample data loading on application startup
- Profile-based configuration (`@Profile("local")`)
- Conditional loading (only if no books exist)

### Database Configuration
- MySQL database with proper indexing
- JPA/Hibernate configuration
- Automatic schema generation

## 🚀 Deployment Ready

The implementation is production-ready with:
- ✅ Comprehensive RBAC implementation
- ✅ Secure API endpoints
- ✅ Proper error handling
- ✅ Data validation
- ✅ Performance optimization
- ✅ Scalable architecture
- ✅ Well-documented code

## 📝 Usage Examples

### Search for Books
```bash
curl -X GET "http://localhost:8080/api/books/search?q=Tolkien" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### Get Book by Barcode
```bash
curl -X GET "http://localhost:8080/api/books/barcode/SAMPLE001" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### Create New Book (Librarian only)
```bash
curl -X POST "http://localhost:8080/api/books" \
  -H "Authorization: Bearer <LIBRARIAN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "New Book",
    "author": "Author Name",
    "barcode": "NEW001",
    "isbn": "978-1234567890"
  }'
```

---

**Implementation Status**: ✅ Complete and Tested  
**RBAC Compliance**: ✅ Fully Implemented  
**Production Ready**: ✅ Yes
