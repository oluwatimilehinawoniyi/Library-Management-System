# Library Management System - JavaFX Frontend

A modern JavaFX desktop application for managing library books, connecting to a Spring Boot REST API backend.

## Features

### Core Requirements
- TableView displaying books from backend
- CRUD operations (Create, Read, Update, Delete)
- Form fields for book data entry
- Backend communication via REST API
- Proper exception handling

### Bonus Features
- **Search functionality** - Filter books by title or author
- **Pagination** - TableView with full pagination support
- **FXML with Scene Builder** - Clean separation of UI and logic
- **CSV Import** - Bulk import books from CSV files
- **Statistics** - View library statistics

### Additional Features
- Modern, responsive UI with custom styling
- Real-time backend connection status
- Input validation with user-friendly error messages
- Sortable table columns
- Status bar with last action tracking
- Configurable page size (5, 10, 20, 50, 100 items)

## Project Structure

```
frontend/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/librarymanagementsystem/frontend/
│       │       ├── LibraryApp.java              # Main application entry point
│       │       ├── controller/
│       │       │   └── MainController.java      # Main UI controller
│       │       ├── model/
│       │       │   ├── BookDTO.java             # Book data transfer object
│       │       │   ├── ApiResponse.java         # API response wrapper
│       │       │   └── PageResponse.java        # Pagination response
│       │       ├── service/
│       │       │   └── BookService.java         # HTTP client service
│       │       └── util/
│       │           └── AlertUtils.java          # Alert dialog utilities
│       └── resources/
│           ├── fxml/
│           │   └── main.fxml                    # Main UI layout
│           └── styles.css                       # Application stylesheet
├── pom.xml                                       # Maven dependencies
└── README.md                                     # This file
```

## Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **Spring Boot Backend** running on `http://localhost:8080`

## Installation & Setup

### 1. Clone the Repository

```bash
cd frontend
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Ensure Backend is Running

Make sure your Spring Boot backend is running on port 8080:

```bash
# In backend directory
mvn spring-boot:run
```

The backend should be accessible at: `http://localhost:8080/api/v1/books`

### 4. Run the Application

**Option A: Using Maven**
```bash
mvn javafx:run
```

**Option B: Using Java directly**
```bash
mvn clean package
java -jar target/library-frontend-1.0.0.jar
```

**Option C: From IDE (IntelliJ IDEA / Eclipse)**
- Right-click on `LibraryApp.java`
- Select "Run 'LibraryApp.main()'"

## Usage Guide

### Adding a Book
1. Click "Add Book" or clear the form
2. Fill in all required fields (Title, Author, ISBN, Published Date)
3. Click "Create Book"

### Updating a Book
1. Select a book from the table
2. Modify the fields in the form
3. Click "Update" or "Save Book"

### Deleting a Book
1. Select a book from the table
2. Click "Delete"
3. Confirm the deletion

### Searching Books
1. Enter search term in the search field (searches title and author)
2. Click "Search" or press Enter
3. Click "Clear" to reset search

### Pagination
- Use navigation buttons: First, Previous, Next, Last
- Change items per page using the dropdown (5, 10, 20, 50, 100)
- View current page and total records in the status bar

### CSV Import
1. Click "Import CSV"
2. Select a CSV file with format: `title,author,isbn,publishedDate`
3. View import results (success/failure counts)

Example CSV:
```csv
title,author,isbn,publishedDate
Clean Code,Robert Martin,978-0132350884,2008-08-01
Design Patterns,Gang of Four,978-0201633610,1994-10-31
```

### View Statistics
- Click "Statistics" to view:
    - Total books count
    - Unique authors count
    - Oldest and newest books

## UI Components

### Main Window Layout
- **Top**: Toolbar with action buttons and search
- **Center Left**: TableView with book listing
- **Center Right**: Form panel for book details
- **Bottom**: Pagination controls and status bar

### Color Scheme
- Primary: Blue (#3498db)
- Success: Green (#27ae60)
- Danger: Red (#e74c3c)
- Dark: Navy (#2c3e50)
- Background: Light gray (#ecf0f1)

## Configuration

### Backend URL
If your backend runs on a different URL, modify `BookService.java`:

```java
private static final String BASE_URL = "http://localhost:8080/api/v1/books";
```

### Default Page Size
To change the default page size, modify `MainController.java`:

```java
private int pageSize = 10; // change value as needed
```

## Testing the Application

### Manual Testing Checklist
- [ ] Add a new book with valid data
- [ ] Add a book with invalid data (future date, empty fields)
- [ ] Update an existing book
- [ ] Delete a book
- [ ] Search for books by title
- [ ] Search for books by author
- [ ] Navigate through pages
- [ ] Change page size
- [ ] Sort by different columns
- [ ] Import CSV with valid data
- [ ] Import CSV with invalid data
- [ ] View statistics
- [ ] Test with backend offline

## API Endpoints Used

| Method | Endpoint                   | Purpose                                            |
|--------|----------------------------|----------------------------------------------------|
| GET    | `/api/v1/books`            | Fetch paginated books                              |
| GET    | `/api/v1/books/{id}`       | Get single book                                    |
| GET    | `/api/v1/books/search`     | Search books                                       |
| POST   | `/api/v1/books`            | Create new book                                    |
| PUT    | `/api/v1/books/{id}`       | Update book                                        |
| DELETE | `/api/v1/books/{id}`       | Delete book                                        |
| POST   | `/api/v1/books/bulk`       | Import CSV                                         |
| POST   | `/api/v1/books/bulk-async` | Import CSV asynchronously (automatically detected) |
| GET    | `/api/v1/books/stats`      | Get statistics                                     |

## Troubleshooting

### Application won't start
- Ensure Java 17+ is installed: `java -version`
- Check Maven is configured: `mvn -version`
- Verify JavaFX is included in dependencies

### Connection Error
- Confirm backend is running: `curl http://localhost:8080/api/v1/books`
- Check firewall settings
- Verify port 8080 is not blocked

### Table not displaying data
- Check browser console for errors
- Verify backend API returns data
- Check pagination settings

### CSV Import fails
- Ensure CSV format matches: `title,author,isbn,publishedDate`
- Check date format is YYYY-MM-DD
- Verify no duplicate ISBNs

## Technologies Used

- **JavaFX 21** - UI framework
- **Java HttpClient** - REST API communication
- **Jackson** - JSON parsing
- **Lombok** - Reduce boilerplate code
- **Maven** - Build tool

## Development

### Using Scene Builder
1. Download JavaFX Scene Builder
2. Open `src/main/resources/fxml/main.fxml`
3. Edit visually
4. Save and rerun application

### Code Style
- Follow JavaFX naming conventions
- FXML IDs use camelCase: `booksTable`, `addButton`
- Event handlers prefixed with `handle`: `handleAdd()`
- Private methods for internal logic

## License

This project is part of a recruitment exercise for demonstration purposes.