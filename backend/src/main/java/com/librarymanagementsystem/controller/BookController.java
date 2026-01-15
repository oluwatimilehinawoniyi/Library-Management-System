package com.librarymanagementsystem.controller;

import com.librarymanagementsystem.commons.dto.BookDTO;
import com.librarymanagementsystem.commons.response.ApiResponse;
import com.librarymanagementsystem.model.ImportJob;
import com.librarymanagementsystem.service.BookService;
import com.librarymanagementsystem.service.ImportJobTracker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
@Tag(name = "Library Book Management", description = "APIs for managing library books")
public class BookController {

    private final BookService bookService;
    private final ImportJobTracker importJobTracker;

    /**
     * Create a new book record
     *
     * @param bookDTO Book data transfer object containing book details
     * @return ResponseEntity with created book wrapped in ApiResponse
     */
    @PostMapping
    @Operation(summary = "Add a new book", description = "Creates a new book record in the library")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Book created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or duplicate ISBN",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Book with ISBN already exists",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<BookDTO>> addBook(@Valid @RequestBody BookDTO bookDTO) {
        log.info("Request to add new book: {}", bookDTO.title());
        BookDTO createdBook = bookService.createBook(bookDTO);
        log.info("Book created successfully with ID: {}", createdBook.id());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdBook, "Book created successfully"));
    }

    /**
     * Get all books with pagination support
     *
     * @param page      Page number (0-indexed)
     * @param size      Number of items per page
     * @param sortBy    Field to sort by (id, title, author, publishedDate)
     * @param direction Sort direction (ASC or DESC)
     * @return ResponseEntity with paginated list of books wrapped in ApiResponse
     */
    @GetMapping
    @Operation(
            summary = "Get all books with pagination",
            description = "Retrieves paginated list of all books in the library. " +
                    "Supports sorting by any book field.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Books retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<Page<BookDTO>>> getAllBooks(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by", example = "title")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "ASC")
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("Request to fetch all books - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, direction);

        Page<BookDTO> books = bookService.getAllBooksPaginated(page, size, sortBy, direction);

        log.info("Retrieved {} books from page {} of {}",
                books.getNumberOfElements(), books.getNumber(), books.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success(
                books,
                String.format("Retrieved %d books", books.getNumberOfElements())
        ));
    }

    /**
     * Get a single book by ID
     *
     * @param id Book ID
     * @return ResponseEntity with book details wrapped in ApiResponse
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Retrieves a specific book by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Book found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<BookDTO>> getBookById(
            @Parameter(description = "ID of the book to retrieve", required = true, example = "1")
            @PathVariable Long id) {

        log.info("Request to fetch book with ID: {}", id);
        BookDTO book = bookService.getBookById(id);

        return ResponseEntity.ok(ApiResponse.success(book, "Book retrieved successfully"));
    }

    /**
     * Search books by title or author with pagination
     *
     * @param keyword Search keyword (case-insensitive partial match)
     * @param page    Page number (0-indexed)
     * @param size    Number of items per page
     * @return ResponseEntity with paginated search results wrapped in ApiResponse
     */
    @GetMapping("/search")
    @Operation(
            summary = "Search books",
            description = "Search books by title or author (case-insensitive partial match). " +
                    "Results are paginated for better performance.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<Page<BookDTO>>> searchBooks(
            @Parameter(description = "Search keyword for title or author", required = true, example = "Martin")
            @RequestParam String keyword,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        log.info("Request to search books with keyword: '{}' - page: {}, size: {}", keyword, page, size);
        Page<BookDTO> books = bookService.searchBooksPaginated(keyword, page, size);

        log.info("Search returned {} books from page {} of {}",
                books.getNumberOfElements(), books.getNumber(), books.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success(
                books,
                String.format("Found %d matching books", books.getTotalElements())
        ));
    }

    /**
     * Update an existing book
     *
     * @param id      Book ID to update
     * @param bookDTO Updated book data
     * @return ResponseEntity with updated book wrapped in ApiResponse
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update book", description = "Updates an existing book's details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Book updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<BookDTO>> updateBook(
            @Parameter(description = "ID of the book to update", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody BookDTO bookDTO) {

        log.info("Request to update book with ID: {}", id);
        BookDTO updatedBook = bookService.updateBook(id, bookDTO);
        log.info("Book updated successfully: {}", updatedBook.title());

        return ResponseEntity.ok(ApiResponse.success(updatedBook, "Book updated successfully"));
    }

    /**
     * Delete a book
     *
     * @param id Book ID to delete
     * @return ResponseEntity with no content (204)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete book", description = "Deletes a book from the library")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Book deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID of the book to delete", required = true, example = "1")
            @PathVariable Long id) {

        log.info("Request to delete book with ID: {}", id);
        bookService.deleteBook(id);
        log.info("Book deleted successfully with ID: {}", id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Bulk import books from CSV file
     * CSV format: title,author,isbn,publishedDate (YYYY-MM-DD)
     * Example: "Clean Code,Robert Martin,978-0132350884,2008-08-01"
     *
     * @param file CSV file containing book data
     * @return ResponseEntity with import results (success/failure counts and error details)
     */
    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Bulk import books from CSV",
            description = "Upload a CSV file to import multiple books at once. " +
                    "Format: title,author,isbn,publishedDate (YYYY-MM-DD). " +
                    "Returns detailed results including success count and any errors encountered.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Books imported successfully (may include partial failures)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid file format or empty file",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> bulkAddBooks(
            @Parameter(description = "CSV file containing book data", required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("Request to bulk import books from file: {}", file.getOriginalFilename());

        // Validate file is not empty
        if (file.isEmpty()) {
            log.warn("Uploaded file is empty");
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("File is empty", "EMPTY_FILE"));
        }

        // Validate file extension
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            log.warn("Invalid file type: {}", filename);
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Only CSV files are supported", "INVALID_FILE_TYPE"));
        }

        // Process the CSV file
        Map<String, Object> result = bookService.bulkCreateBooks(file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Bulk import completed"));
    }

    /**
     * Start async bulk import - returns immediately with job ID
     */
    @PostMapping(value = "/bulk-async", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Async bulk import",
            description = "Starts async import and returns job ID for progress tracking")
    public ResponseEntity<ApiResponse<Map<String, String>>> bulkAddBooksAsync(
            @RequestParam("file") MultipartFile file) {

        log.info("Request to async bulk import: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File is empty", "EMPTY_FILE"));
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Only CSV files are supported", "INVALID_FILE_TYPE"));
        }

        String jobId = bookService.bulkCreateBooksAsync(file);

        return ResponseEntity.accepted()
                .body(ApiResponse.success(
                        Map.of("jobId", jobId, "status", "PROCESSING"),
                        "Import started. Use job ID to check progress."
                ));
    }

    /**
     * Check import job status
     */
    @GetMapping("/bulk/status/{jobId}")
    @Operation(summary = "Get import job status",
            description = "Check progress of an async bulk import")
    public ResponseEntity<ApiResponse<ImportJob>> getImportStatus(@PathVariable String jobId) {
        return importJobTracker.getJob(jobId)
                .map(job -> ResponseEntity.ok(ApiResponse.success(job, "Job status retrieved")))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get library statistics
     * Returns aggregated statistics about the library including:
     * - Total books count
     * - Unique authors count
     * - Books by year distribution
     * - Oldest and newest books
     *
     * @return ResponseEntity with statistics map wrapped in ApiResponse
     */
    @GetMapping("/stats")
    @Operation(
            summary = "Get library statistics",
            description = "Returns comprehensive statistics about the library including " +
                    "total books, unique authors, distribution by year, and oldest/newest books.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        log.info("Request to fetch library statistics");
        Map<String, Object> stats = bookService.getLibraryStats();

        return ResponseEntity.ok(ApiResponse.success(stats, "Statistics retrieved successfully"));
    }
}