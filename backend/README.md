# Library Management System

A comprehensive full-stack application for managing library book records, built with Spring Boot backend and JavaFX
frontend. The system provides complete CRUD operations, advanced search capabilities, bulk CSV import functionality, and
library analytics.

## Table of Contents

- [Project Overview](#project-overview)
- [Technical Stack](#technical-stack)
- [Architecture](#architecture)
- [Features](#features)
- [System Requirements](#system-requirements)
- [Installation and Setup](#installation-and-setup)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Testing](#testing)
- [Usage Guide](#usage-guide)
- [Design Patterns](#design-patterns)
- [Performance Considerations](#performance-considerations)

## Project Overview

This Library Management System demonstrates enterprise-grade software development practices including layered
architecture, comprehensive error handling, API documentation, automated testing, and asynchronous processing for
resource-intensive operations.

The application is designed to handle real-world scenarios such as bulk data imports, concurrent user operations, and
efficient database querying with pagination support.

## Technical Stack

### Backend

- **Framework**: Spring Boot 4.0.1
- **Language**: Java 21
- **Database**: H2
- **ORM**: Spring Data JPA with Hibernate
- **Validation**: Jakarta Bean Validation
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito, AssertJ

### Frontend

- **Framework**: JavaFX 21
- **UI Design**: Scene Builder
- **HTTP Client**: RestTemplate / HttpClient
- **Concurrency**: JavaFX Task API for async operations

### Additional Libraries

- **Lombok**: Boilerplate code reduction
- **MapStruct**: Object mapping
- **OpenCSV**: CSV file processing

## Architecture

The application follows a layered architecture pattern with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│          Presentation Layer             │
│         (JavaFX Controllers)            │
└─────────────────┬───────────────────────┘
                  │ HTTP/REST
┌─────────────────▼───────────────────────┐
│          Controller Layer               │
│      (REST API Endpoints)               │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│          Service Layer                  │
│    (Business Logic & Validation)        │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│          Repository Layer               │
│       (Data Access & Queries)           │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│          Database Layer                 │
│                (H2)                     │
└─────────────────────────────────────────┘
```

### Package Structure

```
com.librarymanagementsystem/
├── controller/          # REST API endpoints
├── service/            # Business logic and validation
│   └── factory/        # Factory pattern implementations
├── repository/         # JPA repositories
├── model/             # JPA entities
├── commons/
│   ├── dto/           # Data Transfer Objects
│   ├── mapper/        # MapStruct mappers
│   └── response/      # API response wrappers
├── exception/         # Custom exception hierarchy
└── config/            # Application configuration
```

## Features

### Core Functionality

1. **CRUD Operations**
    - Create, read, update, and delete book records
    - Field validation and business rule enforcement
    - Duplicate ISBN detection
    - Published date validation (prevents future dates)

2. **Advanced Search**
    - Search books by title or author
    - Case-insensitive partial matching
    - Paginated search results

3. **Pagination Support**
    - Configurable page size
    - Sortable by any field (title, author, published date)
    - Ascending or descending order

4. **Bulk Import**
    - CSV file upload for batch book creation
    - Synchronous import for small files
    - Asynchronous import with progress tracking for large files
    - Comprehensive error reporting with row-level details
    - Validation of all fields before database insertion

5. **Library Analytics**
    - Total book count
    - Unique author statistics
    - Books distribution by publication year
    - Oldest and newest book records

6. **Error Handling**
    - Consistent error response format
    - Field-level validation errors
    - Business logic validation
    - Duplicate resource detection
    - File processing error handling

### Bonus Features Implemented

1. **Search Functionality**: Full-text search across title and author fields with pagination
2. **Pagination**: Complete pagination support for all list endpoints
3. **Scene Builder Integration**: UI designed using JavaFX Scene Builder for rapid development

## System Requirements

### Development Environment

- Java Development Kit (JDK) 21 or higher
- Maven 3.6 or higher
- JavaFX SDK 21 or higher
- IDE: IntelliJ IDEA, Eclipse, or VS Code with Java extensions

## Installation and Setup

### Backend Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/oluwatimilehinawoniyi/Library-Management-System
   cd library-management-system
   cd backend
   ```

2. **Build the Project**
   ```bash
   mvn clean install
   ```

3. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```

   The backend server will start on `http://localhost:8080`

4. **Verify Installation**
    - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
    - H2 Console: `http://localhost:8080/h2-console`
        - JDBC URL: `jdbc:h2:file:./data/librarydb`
        - Username: `sa`
        - Password: (leave empty)

### Frontend Setup

1. **Navigate to Frontend Directory**
   ```bash
   cd frontend
   ```

2. **Build JavaFX Application**
   ```bash
   mvn clean javafx:compile
   ```

3. **Run JavaFX Application**
   ```bash
   mvn javafx:run
   ```

### Configuration

Backend configuration can be modified in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/librarydb
  jpa:
    hibernate:
      ddl-auto: update
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

## API Documentation

### Base URL

```
http://localhost:8080/api/v1
```

### Endpoints

#### Books Management

| Method | Endpoint                     | Description               |
|--------|------------------------------|---------------------------|
| POST   | `/books`                     | Create a new book         |
| GET    | `/books`                     | Get all books (paginated) |
| GET    | `/books/{id}`                | Get book by ID            |
| GET    | `/books/search`              | Search books by keyword   |
| PUT    | `/books/{id}`                | Update book details       |
| DELETE | `/books/{id}`                | Delete a book             |
| POST   | `/books/bulk`                | Bulk import books (sync)  |
| POST   | `/books/bulk-async`          | Bulk import books (async) |
| GET    | `/books/bulk/status/{jobId}` | Check import job status   |
| GET    | `/books/stats`               | Get library statistics    |

#### Request/Response Examples

**Create Book**

```http
POST /api/v1/books
Content-Type: application/json

{
  "title": "Nigerian Constitutional Law",
  "author": "Ben Nwabueze",
  "isbn": "978-9781234567",
  "publishedDate": "1982-06-15"
}
```

**Response**

```json
{
  "success": true,
  "message": "Book created successfully",
  "data": {
    "id": 1,
    "title": "Nigerian Constitutional Law",
    "author": "Ben Nwabueze",
    "isbn": "978-9781234567",
    "publishedDate": "1982-06-15"
  },
  "timestamp": "2026-01-13T10:30:00"
}
```

**Get Books (Paginated)**

```http
GET /api/v1/books?page=0&size=10&sortBy=title&direction=ASC
```

**Response**

```json
{
  "success": true,
  "message": "Retrieved 10 books",
  "data": {
    "content": [
      "..."
    ],
    "pageable": {
      ...
    },
    "totalPages": 5,
    "totalElements": 48,
    "size": 10,
    "number": 0
  },
  "timestamp": "2026-01-13T10:30:00"
}
```

**Bulk Import (CSV)**

```http
POST /api/v1/books/bulk
Content-Type: multipart/form-data

file: books.csv
```

**CSV Format**

```csv
title,author,isbn,publishedDate
Nigerian Constitutional Law,Ben Nwabueze,978-9781234567,1982-06-15
The Law of Evidence in Nigeria,Yadudu A. H.,978-9782345678,1990-03-20
```

**Response**

```json
{
  "success": true,
  "message": "Bulk import completed",
  "data": {
    "successCount": 45,
    "failureCount": 5,
    "totalProcessed": 50,
    "errors": [
      {
        "row": 3,
        "isbn": "INVALID",
        "error": "Invalid ISBN format",
        "errorType": "VALIDATION_ERROR"
      }
    ]
  },
  "timestamp": "2026-01-13T10:35:00"
}
```

### Error Response Format

```json
{
  "success": false,
  "message": "Book with ID 999 not found",
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "details": {
      "resourceType": "Book",
      "resourceId": 999
    }
  },
  "timestamp": "2026-01-13T10:30:00"
}
```

### HTTP Status Codes

| Status Code                  | Description                              |
|------------------------------|------------------------------------------|
| 200 OK                       | Successful GET/PUT request               |
| 201 Created                  | Successful POST request                  |
| 204 No Content               | Successful DELETE request                |
| 400 Bad Request              | Invalid input or validation error        |
| 404 Not Found                | Resource not found                       |
| 409 Conflict                 | Duplicate resource (ISBN already exists) |
| 413 Request Entity Too Large | File size exceeds limit                  |
| 500 Internal Server Error    | Unexpected server error                  |

## Database Schema

### Books Table

```sql
CREATE TABLE books
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(500) NOT NULL,
    author         VARCHAR(255) NOT NULL,
    isbn           VARCHAR(20)  NOT NULL UNIQUE,
    published_date DATE         NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,
    INDEX          idx_title (title),
    INDEX          idx_author (author),
    UNIQUE INDEX idx_isbn (isbn)
);
```

### Field Constraints

| Field          | Type         | Constraints                         |
|----------------|--------------|-------------------------------------|
| id             | BIGINT       | Auto-generated, Primary Key         |
| title          | VARCHAR(500) | NOT NULL, Max 500 characters        |
| author         | VARCHAR(255) | NOT NULL, Max 255 characters        |
| isbn           | VARCHAR(20)  | NOT NULL, UNIQUE, Pattern validated |
| published_date | DATE         | NOT NULL, Cannot be in future       |
| created_at     | TIMESTAMP    | Auto-generated on insert            |
| updated_at     | TIMESTAMP    | Auto-updated on modification        |

## Testing

### Running Tests

**All Tests**

```bash
mvn test
```

**Specific Test Class**

```bash
mvn test -Dtest=BookServiceTest
```

**With Coverage Report**

```bash
mvn clean test jacoco:report
```

View report at: `target/site/jacoco/index.html`

### Test Coverage

- **Unit Tests**: 16 tests covering service layer business logic
- **Total Coverage**: 52%+ line coverage, 46%+ branch coverage
- **NOTE** Tests focus on critical paths and edge cases

### Test Categories

1. **CRUD Operations**
    - Create (success, duplicate ISBN, future date, validation errors)
    - Read (by ID, paginated list, search, not found scenarios)
    - Update (success, not found, duplicate ISBN, validation)
    - Delete (success, not found)

2. **Bulk Import**
    - Successful import
    - Import with errors (detailed error reporting)
    - Empty file rejection
    - Invalid file type rejection

3. **Error Handling**
    - Resource not found (404)
    - Duplicate resources (409)
    - Validation errors (400)
    - Business logic violations (400)

4. **Statistics**
    - Statistics with data
    - Empty library scenario

## Usage Guide

### Creating a Book

Using the JavaFX application:

1. Fill in the book details in the form fields
2. Click "Add Book" button
3. Book appears in the table view
4. Success notification displays

Using the API:

```bash
curl -X POST http://localhost:8080/api/v1/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Nigerian Constitutional Law",
    "author": "Ben Nwabueze",
    "isbn": "978-9781234567",
    "publishedDate": "1982-06-15"
  }'
```

### Searching Books

1. Enter search keyword in the search field
2. Results filter automatically (client-side filtering for loaded data)
3. Use backend search endpoint for large datasets

### Bulk Import

1. Prepare CSV file with format: `title,author,isbn,publishedDate`
2. Click "Import CSV" button in JavaFX application
3. Select CSV file from file chooser
4. View progress bar during import
5. Review import results with success/failure counts

**For Async Import**:

1. Import starts immediately, returns job ID
2. Progress bar shows real-time progress
3. UI remains responsive during import
4. Completion notification when finished

### Pagination Navigation

1. Use "Previous" and "Next" buttons to navigate pages
2. Select page size from dropdown (10, 20, 50, 100)
3. Choose sort field and direction
4. Table updates automatically

## Design Patterns

The application implements several design patterns for maintainability and scalability:

### 1. Repository Pattern

Data access abstraction through Spring Data JPA repositories, separating persistence logic from business logic.

### 2. Data Transfer Object (DTO) Pattern

Separation of internal entity models from API contract models, preventing tight coupling and enabling independent
evolution.

### 3. Builder Pattern

Fluent object construction using Lombok's `@Builder` annotation for complex object creation.

### 4. Factory Pattern

`ImportResultFactory` standardizes the creation of bulk import result responses.

### 5. Singleton Pattern

Spring-managed beans are singletons by default, ensuring single instance of services and repositories.

### 6. Template Method Pattern

Spring's `RestTemplate` and JPA's `JpaRepository` provide template methods for common operations.

## Performance Considerations

### Database Optimizations

1. **Indexed Columns**: Title, author, and ISBN columns are indexed for fast lookups
2. **Unique Constraint**: ISBN has unique index preventing duplicate checks from scanning entire table
3. **Query Optimization**: Custom queries use targeted selection instead of fetch-all operations
4. **Connection Pooling**: HikariCP connection pool for efficient database connections

### Application Optimizations

1. **Pagination**: All list endpoints support pagination to prevent memory issues with large datasets
2. **Lazy Loading**: Relationships would use lazy loading in production scenarios
3. **Async Processing**: Large CSV imports run asynchronously to prevent UI blocking
4. **Batch Processing**: Bulk operations process records in batches with periodic commits

## License

This project is developed as a recruitment exercise demonstrating full-stack development capabilities with Spring Boot
and JavaFX.

---

**Note**: This is a test project showcasing enterprise software development practices including clean
architecture, comprehensive testing, proper error handling, and API documentation.