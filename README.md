# HuSpark - Library Management System

A basic Spring Boot application implementing user management and JWT-based authentication for a library management system.

## Features

- ✅ User Registration
- ✅ Basic Authentication
- ✅ Role-based Access Control (MEMBER, LIBRARIAN, ADMIN)
- ✅ Password Encryption (BCrypt)
- ✅ Input Validation
- ✅ Global Exception Handling
- ✅ Swagger API Documentation
- ✅ Comprehensive Test Coverage

## Technology Stack

- **Framework**: Spring Boot 3.4.8
- **Database**: PostgreSQL (Production), H2 (Testing)
- **Security**: Spring Security (Basic)
- **Build Tool**: Maven
- **Java Version**: 17
- **Testing**: JUnit 5, Mockito
- **Documentation**: Swagger/OpenAPI 3

## Project Structure

```
src/
├── main/
│   ├── java/com/hashedin/huspark/
│   │   ├── config/          # Security and configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # JPA entities
│   │   ├── exception/      # Custom exceptions and global handler
│   │   ├── repository/     # Data access layer
│   │   ├── service/        # Business logic layer
│   │   └── util/           # Utility classes (JWT)
│   └── resources/
│       ├── application.properties
│       └── application-local.properties
└── test/
    ├── java/com/hashedin/huspark/
    │   ├── controller/     # Controller tests
    │   ├── repository/     # Repository tests
    │   └── service/        # Service tests
    └── resources/
        └── application-test.properties
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+ (for production)

## Setup Instructions

### 1. Database Setup

#### For Local Development:
```sql
-- Create database
CREATE DATABASE postgres;

-- Create user (if needed)
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE postgres TO postgres;
```

#### For Production:
Set the following environment variables:
- `DB_HOST`: Database host
- `DB_PORT`: Database port (default: 5432)
- `DB_NAME`: Database name
- `DB_USER`: Database username
- `DB_PASSWORD`: Database password

### 2. Run the Application

#### Using Maven:
```bash
# For local development
mvn spring-boot:run -Dspring-boot.run.profiles=local

# For production
mvn spring-boot:run
```

#### Using JAR:
```bash
# Build the application
mvn clean package

# Run with local profile
java -jar -Dspring.profiles.active=local target/huspark-8.0.1-SNAPSHOT.jar

# Run with production profile
java -jar target/huspark-8.0.1-SNAPSHOT.jar
```

### 3. Access the Application

- **Application URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html (if enabled)
- **API Documentation**: http://localhost:8080/v3/api-docs (if enabled)

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| POST | `/api/auth/register` | Register a new user | Public |

### User Management Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| GET | `/api/users/{id}` | Get user by ID | Public |
| GET | `/api/users/email/{email}` | Get user by email | Public |

## Sample API Usage

### 1. Register a User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "password": "password123"
  }'
```

### 2. Get User by ID
```bash
curl -X GET http://localhost:8080/api/users/1
```

### 3. Get User by Email
```bash
curl -X GET http://localhost:8080/api/users/email/john.doe@example.com
```

## Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=UserServiceTest
```

### Test Coverage
The project includes comprehensive tests for:
- **Repository Layer**: Data access operations
- **Service Layer**: Business logic
- **Controller Layer**: API endpoints
- **Integration Tests**: Full application context

## User Roles

- **MEMBER**: Default role for new users, basic access
- **LIBRARIAN**: Can view user information
- **ADMIN**: Full access to all user operations

## Security Features

- **Password Encryption**: BCrypt hashing
- **Basic Authentication**: Simple security setup
- **Role-based Authorization**: User roles (MEMBER, LIBRARIAN, ADMIN)
- **Input Validation**: Request/Response validation
- **Exception Handling**: Global error handling

## Configuration

### Application Properties

#### Local Development (`application-local.properties`)
```properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.show_sql=true
```

#### Production (`application.properties`)
Uses environment variables for database configuration.

## Error Handling

The application includes comprehensive error handling:
- **400 Bad Request**: Validation errors
- **401 Unauthorized**: Authentication failures
- **403 Forbidden**: Authorization failures
- **404 Not Found**: Resource not found
- **409 Conflict**: User already exists
- **500 Internal Server Error**: Unexpected errors

## Development Notes

- Default user role is `MEMBER`
- Passwords must be at least 6 characters
- Email addresses must be unique
- Simple authentication without JWT complexity

## Future Enhancements

- Password reset functionality
- Email verification
- User profile updates
- Admin user management
- Audit logging
- Rate limiting

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is part of the HashedIn training program.
