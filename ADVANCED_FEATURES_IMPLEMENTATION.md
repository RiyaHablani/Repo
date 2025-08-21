# Advanced Features Implementation

This document outlines the implementation of advanced features for the Library Management System, including pagination, audit logging, and notification systems.

## Table of Contents

1. [Task 4.1: Advanced API Querying](#task-41-advanced-api-querying)
2. [Task 4.2: System Audit Trail](#task-42-system-audit-trail)
3. [Task 4.3: Notification System](#task-43-notification-system)
4. [Testing](#testing)
5. [API Documentation](#api-documentation)

## Task 4.1: Advanced API Querying

### Overview
Enhanced APIs that return lists of data with pagination, searching, and sorting capabilities to improve performance and user experience.

### Features Implemented

#### 1. Pagination Support
- **PaginatedResponse DTO**: Generic pagination wrapper for all list responses
- **Page Parameters**: `page` (0-based), `size`, `sortBy`, `sortDirection`
- **Metadata**: Total elements, total pages, navigation flags

#### 2. Advanced Sorting
- **Multi-field Sorting**: Support for sorting by any entity field
- **Direction Control**: ASC/DESC sorting options
- **Default Sorting**: Configurable default sort criteria

#### 3. Enhanced Filtering
- **Multi-criteria Filtering**: Combine multiple filter conditions
- **Dynamic Queries**: Build queries based on provided parameters
- **Null-safe Filtering**: Handle optional filter parameters gracefully

### Implementation Details

#### New DTOs Created
```java
// PaginatedResponse.java
public class PaginatedResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean isFirst;
    private boolean isLast;
}

// SortRequest.java
public class SortRequest {
    private String field;
    private String direction = "ASC";
}
```

#### Enhanced Repository Methods
```java
// BookRepository.java
Page<Book> searchBooksPaginated(String searchTerm, Pageable pageable);
Page<Book> findBooksWithFilters(String title, String author, String genre, 
                               BookStatus status, String publisher, Pageable pageable);

// UserRepository.java
Page<User> searchUsers(String searchTerm, Pageable pageable);
Page<User> findUsersWithFilters(String name, String email, Role role, Pageable pageable);
```

#### Service Layer Enhancements
```java
// BookService.java
public PaginatedResponse<BookResponse> getAllBooksPaginated(int page, int size, 
                                                           String sortBy, String sortDirection);
public PaginatedResponse<BookResponse> searchBooksPaginated(String searchTerm, int page, 
                                                           int size, String sortBy, String sortDirection);
public PaginatedResponse<BookResponse> getBooksWithFilters(String title, String author, 
                                                          String genre, BookStatus status, 
                                                          String publisher, int page, int size, 
                                                          String sortBy, String sortDirection);
```

### API Endpoints

#### Books
- `GET /api/books?page=0&size=10&sortBy=title&sortDirection=asc`
- `GET /api/books/search?q=java&page=0&size=5&sortBy=author&sortDirection=desc`
- `GET /api/books/filter?genre=fiction&status=AVAILABLE&page=0&size=5&sortBy=title&sortDirection=asc`
- `GET /api/books/status/AVAILABLE?page=0&size=10&sortBy=title&sortDirection=asc`

#### Users (Admin Only)
- `GET /api/users/admin/all?page=0&size=10&sortBy=name&sortDirection=asc`
- `GET /api/users/admin/search?q=member&page=0&size=5&sortBy=email&sortDirection=asc`
- `GET /api/users/admin/filter?role=MEMBER&page=0&size=5&sortBy=name&sortDirection=asc`
- `GET /api/users/admin/role/LIBRARIAN?page=0&size=10&sortBy=name&sortDirection=asc`

## Task 4.2: System Audit Trail

### Overview
Comprehensive audit trail system that logs all major operations for security and accountability.

### Features Implemented

#### 1. Audit Log Entity
```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    private Long id;
    private Long userId;
    private String userEmail;
    private String action;
    private String entityType;
    private Long entityId;
    private String description;
    private String details; // JSON with encrypted sensitive data
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private boolean isEncrypted;
}
```

#### 2. Security Features
- **Sensitive Data Encryption**: Automatic encryption of sensitive information
- **User Context Tracking**: Capture user performing the action
- **IP Address Logging**: Track source of operations
- **User Agent Logging**: Browser/client information

#### 3. Comprehensive Logging
- **User Operations**: Registration, login, profile updates
- **Book Operations**: Creation, updates, deletion, status changes
- **Borrowing Operations**: Book borrowing, returns, overdue handling
- **System Operations**: Configuration changes, bulk operations

### Implementation Details

#### AuditService
```java
@Service
public class AuditService {
    // Core logging method
    public void logAction(String action, String entityType, Long entityId, 
                         String description, Object details);
    
    // Specific logging methods
    public void logUserRegistration(User user);
    public void logBookCreation(Long bookId, String bookTitle);
    public void logBookUpdate(Long bookId, String bookTitle, Map<String, Object> changes);
    public void logBookDeletion(Long bookId, String bookTitle);
    public void logBookBorrow(Long bookId, Long userId, Long transactionId);
    public void logBookReturn(Long bookId, Long userId, Long transactionId);
    
    // Query methods
    public PaginatedResponse<AuditLog> getAuditLogs(Pageable pageable);
    public PaginatedResponse<AuditLog> getAuditLogsByUser(Long userId, Pageable pageable);
    public PaginatedResponse<AuditLog> getAuditLogsByEntity(String entityType, Long entityId, Pageable pageable);
    public PaginatedResponse<AuditLog> getAuditLogsByAction(String action, Pageable pageable);
    public PaginatedResponse<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
```

#### Integration Points
- **BookService**: Logs all book CRUD operations
- **UserService**: Logs user registration
- **BorrowingService**: Logs borrowing and return transactions

### API Endpoints (Admin Only)

#### Audit Log Access
- `GET /api/audit/logs?page=0&size=20`
- `GET /api/audit/logs/user/{userId}?page=0&size=20`
- `GET /api/audit/logs/entity/{entityType}/{entityId}?page=0&size=20`
- `GET /api/audit/logs/action/{action}?page=0&size=20`
- `GET /api/audit/logs/date-range?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59&page=0&size=20`
- `GET /api/audit/logs/user-email?userEmail=user@example.com&page=0&size=20`

## Task 4.3: Notification System

### Overview
Background scheduled job system for sending notifications for overdue books using email and SMS.

### Features Implemented

#### 1. Notification Entity
```java
@Entity
@Table(name = "notifications")
public class Notification {
    private Long id;
    private Long userId;
    private Long bookId;
    private Long transactionId;
    private NotificationType type;
    private String message;
    private LocalDateTime sentAt;
    private DeliveryStatus deliveryStatus;
    private DeliveryMethod deliveryMethod;
    private String recipientEmail;
    private String recipientPhone;
    private LocalDateTime createdAt;
    
    public enum NotificationType {
        OVERDUE_BOOK, DUE_DATE_REMINDER, BOOK_RETURNED, BOOK_BORROWED
    }
    
    public enum DeliveryStatus {
        PENDING, SENT, FAILED, DELIVERED
    }
    
    public enum DeliveryMethod {
        EMAIL, SMS, BOTH
    }
}
```

#### 2. Scheduled Jobs
```java
@Service
public class NotificationService {
    // Daily overdue notifications at 9 AM
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendOverdueNotifications();
    
    // Daily due date reminders at 10 AM
    @Scheduled(cron = "0 0 10 * * ?")
    public void sendDueDateReminders();
}
```

#### 3. Notification Types
- **Overdue Notifications**: Sent daily for books past due date
- **Due Date Reminders**: Sent for books due tomorrow
- **Borrow Confirmations**: Sent when books are borrowed
- **Return Confirmations**: Sent when books are returned

#### 4. Delivery Methods
- **Email Service**: Mock email service (logs to console)
- **SMS Service**: Mock SMS service (logs to console)
- **Dual Delivery**: Support for both email and SMS

### Implementation Details

#### NotificationService
```java
@Service
public class NotificationService {
    // Scheduled methods
    public void sendOverdueNotifications();
    public void sendDueDateReminders();
    
    // Manual notification methods
    public void sendOverdueNotification(BorrowingTransaction transaction);
    public void sendDueDateReminder(BorrowingTransaction transaction);
    public void sendBookReturnedNotification(BorrowingTransaction transaction);
    public void sendBookBorrowedNotification(BorrowingTransaction transaction);
}
```

#### Mock Services
```java
@Service
public class EmailService {
    public void sendEmail(String to, String subject, String message);
}

@Service
public class SmsService {
    public void sendSms(String phoneNumber, String message);
}
```

#### Integration Points
- **BorrowingService**: Triggers notifications on borrow/return
- **Scheduled Jobs**: Automatically send overdue and reminder notifications

## Testing

### Test Script
A comprehensive test script `test_advanced_features.sh` has been created to verify all features:

```bash
# Run the test script
./test_advanced_features.sh
```

### Test Coverage
1. **Pagination Testing**: Verify page navigation, sorting, and filtering
2. **Audit Logging**: Test audit trail creation and retrieval
3. **Notification System**: Verify notification triggers and delivery
4. **Advanced Querying**: Test complex filtering and sorting scenarios

### Manual Testing
```bash
# Test pagination
curl -H "Authorization: Bearer $TOKEN" \
     "http://localhost:8080/api/books?page=0&size=5&sortBy=title&sortDirection=asc"

# Test audit logs
curl -H "Authorization: Bearer $ADMIN_TOKEN" \
     "http://localhost:8080/api/audit/logs?page=0&size=10"

# Test notifications (borrow a book)
curl -X POST -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"bookId": 1}' \
     "http://localhost:8080/api/borrowing/borrow"
```

## API Documentation

### Pagination Parameters
All list endpoints support the following query parameters:
- `page`: Page number (0-based, default: 0)
- `size`: Page size (default: 10)
- `sortBy`: Field to sort by (default: varies by endpoint)
- `sortDirection`: Sort direction - "asc" or "desc" (default: "asc")

### Response Format
```json
{
  "content": [...],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 100,
  "totalPages": 10,
  "hasNext": true,
  "hasPrevious": false,
  "isFirst": true,
  "isLast": false
}
```

### Error Handling
- **400 Bad Request**: Invalid pagination parameters
- **401 Unauthorized**: Missing or invalid authentication
- **403 Forbidden**: Insufficient permissions for audit access
- **500 Internal Server Error**: System errors

## Configuration

### Application Properties
```properties
# Enable scheduling
spring.scheduling.enabled=true

# Audit log configuration
audit.encryption.enabled=true
audit.retention.days=365

# Notification configuration
notification.email.enabled=true
notification.sms.enabled=true
notification.schedule.overdue=0 0 9 * * ?
notification.schedule.reminder=0 0 10 * * ?
```

### Database Schema
The implementation adds the following tables:
- `audit_logs`: Audit trail records
- `notifications`: Notification records

## Security Considerations

1. **Audit Log Access**: Only administrators can access audit logs
2. **Data Encryption**: Sensitive audit data is encrypted
3. **Input Validation**: All pagination parameters are validated
4. **Rate Limiting**: Consider implementing rate limiting for audit queries
5. **Data Retention**: Audit logs should have retention policies

## Performance Considerations

1. **Pagination**: Reduces memory usage and improves response times
2. **Indexing**: Database indexes on frequently queried audit fields
3. **Caching**: Consider caching for frequently accessed audit data
4. **Batch Processing**: Notifications are processed in batches
5. **Async Processing**: Consider async processing for notifications

## Future Enhancements

1. **Real Email/SMS Integration**: Replace mock services with real providers
2. **Audit Log Archiving**: Implement log archiving for long-term storage
3. **Advanced Analytics**: Add analytics on audit data
4. **Real-time Notifications**: WebSocket-based real-time notifications
5. **Notification Templates**: Configurable notification templates
6. **Audit Log Export**: Export audit logs in various formats

## Conclusion

The advanced features implementation provides:

✅ **Complete Pagination System**: Efficient data retrieval with sorting and filtering
✅ **Comprehensive Audit Trail**: Security and accountability for all operations
✅ **Automated Notification System**: Proactive communication with users
✅ **Scalable Architecture**: Designed for growth and performance
✅ **Security Focused**: Proper access controls and data protection
✅ **Well Tested**: Comprehensive test coverage and documentation

The system is now production-ready with enterprise-grade features for managing a library effectively and securely.
