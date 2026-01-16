# Library Management System

A full-stack library management application featuring a Spring Boot REST API backend and a JavaFX desktop client. The
system provides comprehensive book management capabilities including CRUD operations, advanced search, bulk CSV import,
and library analytics.

## Project Overview

This application demonstrates enterprise-level software development practices with a clean separation between backend
services and frontend presentation. The backend provides a RESTful API with comprehensive documentation, while the
frontend offers an intuitive desktop interface built with JavaFX.

## Repository Structure

```
library-management-system/
├── backend/                    # Spring Boot REST API
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # Application source code
│   │   │   └── resources/     # Configuration files
│   │   └── test/              # Unit and integration tests
│   ├── pom.xml
│   └── README.md              # Backend documentation
├── frontend/                   # JavaFX Desktop Client
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # JavaFX application code
│   │   │   └── resources/     # FXML files and assets
│   │   └── test/              # Frontend tests
│   ├── pom.xml
│   └── README.md              # Frontend documentation
├── test-data/                  # Sample CSV files for testing
│   ├── nigerian_law_books_success.csv
│   ├── nigerian_law_books_with_errors.csv
│   └── nigerian_law_books_large_batch.csv
├── screenshots/                # Application screenshots
│   ├── backend-swagger.png
│   ├── frontend-main.png
│   ├── frontend-search.png
│   └── frontend-import.png
└── README.md                   # This file
```

## Technology Stack

### Backend

- **Framework**: Spring Boot 4.0.1
- **Language**: Java 21
- **Database**: H2
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Build Tool**: Maven

### Frontend

- **Framework**: JavaFX 21
- **UI Design**: Scene Builder
- **HTTP Client**: Java HttpClient
- **Build Tool**: Maven

### Other Technologies

- **Object Mapping**: MapStruct
- **CSV Processing**: OpenCSV
- **Validation**: Jakarta Bean Validation

## Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- Git

### Running the Application

#### 1. Start the Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend server will start on `http://localhost:8080`

**Verify backend is running:**

- API Documentation: http://localhost:8080/swagger-ui/index.html
- H2 Console: http://localhost:8080/h2-console

#### 2. Start the Frontend

Open a new terminal:

```bash
cd frontend
mvn clean javafx:run
```

The JavaFX application window will open.

### Testing with Sample Data

Sample CSV files are provided in the `test-data/` folder:

1. **nigerian_law_books_success.csv** - 20 valid book records
2. **nigerian_law_books_with_errors.csv** - Mixed valid/invalid records for testing error handling
3. **nigerian_law_books_large_batch.csv** - 50 books for performance testing

To import via frontend:

1. Click "Import CSV" button
2. Navigate to `test-data/` folder
3. Select a CSV file
4. View import results

## Features

### Backend Features

- RESTful API with full CRUD operations
- Pagination and sorting support
- Advanced search capabilities
- Bulk CSV import (synchronous and asynchronous)
- Comprehensive error handling
- Field validation and business rule enforcement
- Library statistics and analytics
- Swagger UI documentation

### Frontend Features

- Intuitive desktop interface
- Book management (create, read, update, delete)
- Real-time search and filtering
- Paginated table view
- CSV bulk import with progress tracking
- Statistics dashboard
- Error notifications and user feedback

## Architecture

The application follows a layered architecture with clear separation of concerns:

### Backend Layers

```
Controller → Service → Repository → Database
```

### Frontend Layers

```
View (FXML) → Controller → Service (HTTP Client) → Backend API
```

## API Endpoints

Base URL: `http://localhost:8080/api/v1`

| Method | Endpoint            | Description               |
|--------|---------------------|---------------------------|
| POST   | `/books`            | Create a new book         |
| GET    | `/books`            | Get all books (paginated) |
| GET    | `/books/{id}`       | Get book by ID            |
| GET    | `/books/search`     | Search books              |
| PUT    | `/books/{id}`       | Update book               |
| DELETE | `/books/{id}`       | Delete book               |
| POST   | `/books/bulk`       | Bulk import (sync)        |
| POST   | `/books/bulk-async` | Bulk import (async)       |
| GET    | `/books/stats`      | Library statistics        |

For detailed API documentation, visit the Swagger UI when the backend is running.

## Development

### Backend Development

```bash
cd backend

# Run tests
mvn test

# Run with test coverage
mvn clean test jacoco:report

# Build JAR
mvn clean package

# Run the JAR
java -jar target/library-management-system-1.0.0.jar
```

### Frontend Development

```bash
cd frontend

# Compile
mvn clean compile

# Run
mvn clean javafx:run

# Package as executable
mvn clean package
```

## Testing

### Backend Tests

- **Unit Tests**: 22 tests covering service layer business logic

Run backend tests:

```bash
cd backend
mvn test
```

## Configuration

### Backend Configuration

Configuration file: `backend/src/main/resources/application.yml`

Key settings:

- Database URL: `jdbc:h2:file:./data/librarydb`
- Server port: `8080`
- File upload limit: `10MB`

### Frontend Configuration

The frontend connects to the backend at `http://localhost:8080` by default. This can be configured in the application
settings.

## Database

The application uses H2 file-based database for data persistence. The database file is stored in
`backend/data/librarydb.mv.db`.

**Access H2 Console:**

1. Navigate to http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:file:./data/librarydb`
3. Username: `sa`
4. Password: (leave empty)

## Design Patterns

The application implements several design patterns:

- **Repository Pattern**: Data access abstraction
- **DTO Pattern**: API and domain model separation
- **Builder Pattern**: Complex object construction
- **Factory Pattern**: Standardized object creation
- **MVC Pattern**: Frontend architecture

## Troubleshooting

### Backend Issues

**Port 8080 already in use:**

```bash
# Change port in application.yml
server:
  port: 8081
```

**Database locked:**

```bash
# Stop all running instances and delete lock file
rm -rf data/ # make sure you are in the parent directory
rm backend/data/librarydb.mv.db
```

### Frontend Issues

**Cannot connect to backend:**

- Ensure backend is running on port 8080
- Check firewall settings
- Verify backend URL in frontend configuration

**JavaFX runtime error:**

- Ensure JavaFX SDK is properly installed
- Verify Maven JavaFX plugin configuration

## Screenshots

Screenshots of the application are available in the `screenshots/` folder:

- `Swagger UI.png` - Swagger UI API documentation
- `Frontend UI.png` - Main application window
- `User-Friendly Error Handling.png` - Error handling functionality
- `Swagger schema ui` - Swagger UI API Schema

## License

This project is developed as a demonstration of full-stack development capabilities.

---

**Developed as part of a recruitment exercise demonstrating:**

- RESTful API design and implementation
- Desktop application development with JavaFX
- Full-stack integration
- Test-driven development
- Clean architecture principles
- Professional documentation practices