# Secure Borrowing Transactions System Implementation

## Overview
Successfully implemented a comprehensive secure borrowing transactions system with data integrity, encryption, and role-based access control for the HuSpark library management system.

## 🏗️ Architecture

### Database Schema Extensions

#### BorrowingTransaction Entity
- **Primary Key**: `id` (Auto-generated)
- **Relationships**: 
  - `user` (ManyToOne) - Links to User entity
  - `book` (ManyToOne) - Links to Book entity
- **Transaction Fields**:
  - `borrowedAt` - When the book was borrowed
  - `dueDate` - When the book is due for return
  - `returnedAt` - When the book was returned
  - `status` - Transaction status (BORROWED, RETURNED, OVERDUE, LOST)
  - `lateFee` - Calculated late fees
  - `isOverdue` - Boolean flag for overdue status
  - `reminderSentCount` - Number of reminders sent
  - `lastReminderSent` - Last reminder timestamp
  - `notes` - Additional notes

#### Enhanced Book Entity
- **New Fields**:
  - `isBorrowable` - Boolean flag for non-borrowable books
  - `shelfLocation` - Physical shelf location
  - `rowNumber` - Row position for admin staff
  - `columnNumber` - Column position for admin staff
  - `maxBorrowingDays` - Maximum borrowing period

#### Enhanced User Entity
- **New Fields**:
  - `phoneNumber` - Encrypted contact information
  - `address` - Encrypted address
  - `maxBooksAllowed` - Maximum books user can borrow
  - `lastBorrowingDate` - Last time user borrowed
  - `totalBooksBorrowed` - Total books borrowed historically
  - `overdueCount` - Number of times user was overdue

### TransactionStatus Enum
```java
public enum TransactionStatus {
    BORROWED,    // Book is currently borrowed
    RETURNED,    // Book has been returned
    OVERDUE,     // Book is overdue (past due date)
    LOST         // Book is reported as lost
}
```

## 🔐 Security & Data Protection

### Encryption Implementation
- **AES Encryption** for sensitive user data
- **Encrypted Fields**: phone number, address
- **Key Management**: Configurable secret key
- **Access Control**: Only librarians/admins can decrypt

### Data Integrity Features
- **Prevent Double Borrowing**: Users cannot borrow the same book twice
- **Borrowing Limits**: Configurable maximum books per user
- **Due Date Enforcement**: Automatic overdue detection
- **Status Validation**: Book availability checks
- **Transaction Logging**: Complete audit trail

## 📚 API Endpoints

### Member Endpoints (MEMBER, LIBRARIAN, ADMIN)
```
POST /api/borrowing/borrow              - Borrow a book
POST /api/borrowing/return/{bookId}     - Return a book
GET /api/borrowing/history              - View borrowing history
GET /api/borrowing/current              - View current borrowings
```

### Librarian Endpoints (LIBRARIAN, ADMIN)
```
GET /api/borrowing/overdue              - View overdue transactions
GET /api/borrowing/reminders            - View transactions due for reminders
POST /api/borrowing/reminders/{id}/send - Send reminder
POST /api/borrowing/late-fees/calculate - Calculate late fees
GET /api/borrowing/inactive-users       - Find inactive users
GET /api/borrowing/users/{id}/contact   - Get encrypted user contact info
```

## 🔍 Business Logic Implementation

### Borrowing Process
1. **Validation Checks**:
   - Book exists and is borrowable
   - Book is available (not already borrowed)
   - User hasn't already borrowed this book
   - User hasn't reached borrowing limit
   - User has proper permissions

2. **Transaction Creation**:
   - Create borrowing transaction record
   - Update book status to BORROWED
   - Set due date (default 14 days)
   - Update user statistics

### Returning Process
1. **Validation Checks**:
   - User has borrowed the book
   - Book exists

2. **Late Fee Calculation**:
   - Calculate overdue days
   - Apply daily late fee ($1/day)
   - Update user overdue count

3. **Status Updates**:
   - Mark transaction as RETURNED
   - Update book status to AVAILABLE
   - Record return timestamp

### Reminder System
- **Automatic Detection**: Find books due in 1-3 days
- **Reminder Limits**: Maximum 3 reminders per transaction
- **Frequency Control**: Minimum 1 day between reminders
- **Audit Trail**: Track all reminder activities

### Late Fee Management
- **Automatic Calculation**: $1 per overdue day
- **Batch Processing**: Calculate fees for all overdue returns
- **User Tracking**: Maintain overdue count per user

## 🛡️ Security Features

### Role-Based Access Control (RBAC)
- **MEMBERS**: Can borrow/return books, view their history
- **LIBRARIANS**: Can manage transactions, send reminders, view reports
- **ADMINS**: Full access to all features

### Data Protection
- **Encrypted Storage**: Sensitive user data encrypted at rest
- **Access Control**: Only authorized staff can decrypt data
- **Audit Logging**: All transactions logged with timestamps

### Input Validation
- **Request Validation**: All inputs validated with @Valid
- **Business Rule Validation**: Custom validation for borrowing rules
- **Error Handling**: Comprehensive exception handling

## 📊 Data Transfer Objects (DTOs)

### BorrowRequest
```java
public class BorrowRequest {
    @NotNull private Long bookId;
    private LocalDateTime dueDate; // Optional
}
```

### BorrowingTransactionResponse
```java
public class BorrowingTransactionResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long bookId;
    private String bookTitle;
    private String bookBarcode;
    private LocalDateTime borrowedAt;
    private LocalDateTime dueDate;
    private LocalDateTime returnedAt;
    private TransactionStatus status;
    private BigDecimal lateFee;
    private boolean isOverdue;
    private Integer reminderSentCount;
    private LocalDateTime lastReminderSent;
    private String notes;
}
```

## 🗄️ Repository Layer

### BorrowingTransactionRepository
- **Custom Queries**:
  - Find active borrowings by user/book
  - Find overdue transactions
  - Find transactions due for reminders
  - Count active borrowings per user
  - Find inactive users
  - Find transactions needing late fee calculation

### Key Methods
```java
Optional<BorrowingTransaction> findByUserIdAndBookIdAndStatusIn(...);
List<BorrowingTransaction> findOverdueTransactions(LocalDateTime now);
List<BorrowingTransaction> findTransactionsDueForReminders(...);
Long countActiveBorrowingsByUserId(Long userId);
List<Long> findInactiveUserIds(LocalDateTime thresholdDate);
```

## 🎯 Service Layer

### BorrowingService
- **Core Features**:
  - Book borrowing with validation
  - Book returning with late fee calculation
  - Transaction history management
  - Reminder system management
  - Late fee calculation
  - Encrypted data access

### Key Business Logic
- **Borrowing Validation**: Multiple checks before allowing borrowing
- **Late Fee Calculation**: Automatic calculation based on overdue days
- **Status Management**: Automatic status updates based on due dates
- **Reminder System**: Intelligent reminder scheduling
- **Data Encryption**: Secure handling of sensitive information

## 🚨 Exception Handling

### Custom Exceptions
- `BookNotBorrowableException` - Book cannot be borrowed
- `BookNotAvailableException` - Book is not available
- `BookAlreadyBorrowedException` - User already borrowed this book
- `BorrowingLimitExceededException` - User reached borrowing limit
- `BookNotBorrowedException` - User hasn't borrowed this book
- `TransactionNotFoundException` - Transaction not found
- `MaxRemindersExceededException` - Too many reminders sent

### Global Exception Handler
- **HTTP Status Codes**: Proper status codes for each exception type
- **Error Messages**: Clear, user-friendly error messages
- **Logging**: Comprehensive error logging

## 🔧 Configuration

### Application Properties
```properties
# Encryption
app.encryption.secret=your-secret-key-here

# Borrowing Settings
default.borrowing.days=14
daily.late.fee=1.00
max.reminders=3
inactive.user.threshold.months=6
```

### Database Configuration
- **Schema Generation**: Automatic table creation
- **Foreign Key Constraints**: Proper referential integrity
- **Indexing**: Optimized for query performance

## 📈 Features Implemented

### ✅ Core Functionality
1. **Secure Borrowing**: Complete borrowing workflow with validation
2. **Book Returning**: Return process with late fee calculation
3. **Transaction Tracking**: Complete audit trail of all activities
4. **Status Management**: Automatic status updates
5. **Due Date Enforcement**: Overdue detection and handling

### ✅ Advanced Features
1. **Reminder System**: Automated reminder management
2. **Late Fee Calculation**: Automatic fee calculation
3. **Inactive User Detection**: Find users who haven't borrowed recently
4. **Book Positioning**: Admin tools for physical book location
5. **Non-borrowable Books**: Support for reference materials
6. **Borrowing Limits**: Configurable limits per user

### ✅ Security & Data Protection
1. **Encryption**: AES encryption for sensitive data
2. **RBAC**: Role-based access control
3. **Data Integrity**: Comprehensive validation
4. **Audit Logging**: Complete transaction history
5. **Error Handling**: Robust exception management

### ✅ Admin & Management Features
1. **Overdue Reports**: View all overdue transactions
2. **Reminder Management**: Send and track reminders
3. **Late Fee Processing**: Batch late fee calculation
4. **User Analytics**: Track user borrowing patterns
5. **Contact Information**: Secure access to user details

## 🚀 Deployment Ready

The implementation is production-ready with:
- ✅ Comprehensive security measures
- ✅ Data integrity protection
- ✅ Scalable architecture
- ✅ Proper error handling
- ✅ Complete audit trail
- ✅ Performance optimization
- ✅ Well-documented code

## 📝 Usage Examples

### Borrow a Book
```bash
curl -X POST "http://localhost:8080/api/borrowing/borrow" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"bookId": 1}'
```

### Return a Book
```bash
curl -X POST "http://localhost:8080/api/borrowing/return/1" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### View Overdue Transactions (Librarian)
```bash
curl -X GET "http://localhost:8080/api/borrowing/overdue" \
  -H "Authorization: Bearer <LIBRARIAN_TOKEN>"
```

### Send Reminder (Librarian)
```bash
curl -X POST "http://localhost:8080/api/borrowing/reminders/1/send" \
  -H "Authorization: Bearer <LIBRARIAN_TOKEN>"
```

---

**Implementation Status**: ✅ Complete and Tested  
**Security Compliance**: ✅ Fully Implemented  
**Data Integrity**: ✅ Guaranteed  
**Production Ready**: ✅ Yes
