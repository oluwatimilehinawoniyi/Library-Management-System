package com.librarymanagementsystem.service;

import com.librarymanagementsystem.commons.dto.BookDTO;
import com.librarymanagementsystem.commons.dto.ImportError;
import com.librarymanagementsystem.commons.mapper.BookMapper;
import com.librarymanagementsystem.exception.*;
import com.librarymanagementsystem.model.Book;
import com.librarymanagementsystem.model.ImportJob;
import com.librarymanagementsystem.repository.BookRepository;
import com.librarymanagementsystem.service.factory.ImportResultFactory;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final ImportJobTracker importJobTracker;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Create a new book
     * Validates business rules and checks for duplicate ISBN
     *
     * @param bookDTO Book data to create
     * @return Created book DTO
     * @throws DuplicateResourceException if ISBN already exists
     * @throws BusinessLogicException     if business validation fails
     */
    public BookDTO createBook(BookDTO bookDTO) {
        log.info("Creating book: {}", bookDTO.title());

        // Business validation: Check future dates
        validatePublishedDate(bookDTO.publishedDate());

        // Check for duplicate ISBN
        if (bookRepository.existsByIsbn(bookDTO.isbn())) {
            log.warn("Attempted to create book with duplicate ISBN: {}", bookDTO.isbn());
            throw new DuplicateResourceException("ISBN", bookDTO.isbn());
        }

        // Map DTO to entity and save
        Book book = bookMapper.toEntity(bookDTO);
        Book savedBook = bookRepository.save(book);

        log.info("Book created successfully with ID: {}", savedBook.getId());
        return bookMapper.toDTO(savedBook);
    }

    /**
     * Get all books with pagination and sorting
     *
     * @param page      Page number (0-indexed)
     * @param size      Number of items per page
     * @param sortBy    Field to sort by
     * @param direction Sort direction (ASC or DESC)
     * @return Paginated list of books
     */
    @Transactional(readOnly = true)
    public Page<BookDTO> getAllBooksPaginated(int page, int size, String sortBy, String direction) {
        log.info("Fetching books - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, direction);

        // Validate page and size
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10; // Max 100 items per page

        // Create sort object
        Sort sort = direction.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        // Create pageable and fetch
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Book> bookPage = bookRepository.findAll(pageable);

        // Map to DTOs
        return bookPage.map(bookMapper::toDTO);
    }

    /**
     * Get a single book by ID
     *
     * @param id Book ID
     * @return Book DTO
     * @throws ResourceNotFoundException if book not found
     */
    @Transactional(readOnly = true)
    public BookDTO getBookById(Long id) {
        log.info("Fetching book with ID: {}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Book not found with ID: {}", id);
                    return new ResourceNotFoundException("Book", id);
                });

        return bookMapper.toDTO(book);
    }

    /**
     * Search books by title or author with pagination
     *
     * @param keyword Search keyword
     * @param page    Page number
     * @param size    Page size
     * @return Paginated search results
     */
    @Transactional(readOnly = true)
    public Page<BookDTO> searchBooksPaginated(String keyword, int page, int size) {
        log.info("Searching books with keyword: '{}' - page: {}, size: {}", keyword, page, size);

        // Validate parameters
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBooksPaginated(page, size, "id", "ASC");
        }

        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.searchByTitleOrAuthor(keyword.trim(), pageable);

        return bookPage.map(bookMapper::toDTO);
    }

    /**
     * Update an existing book
     *
     * @param id      Book ID to update
     * @param bookDTO Updated book data
     * @return Updated book DTO
     * @throws ResourceNotFoundException  if book not found
     * @throws DuplicateResourceException if ISBN change conflicts with existing book
     */
    public BookDTO updateBook(Long id, BookDTO bookDTO) {
        log.info("Updating book with ID: {}", id);

        // Find existing book
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Book not found with ID: {}", id);
                    return new ResourceNotFoundException("Book", id);
                });

        // Business validation
        validatePublishedDate(bookDTO.publishedDate());

        String normalizedExistingIsbn = existingBook.getIsbn().trim().toUpperCase();
        String normalizedNewIsbn = bookDTO.isbn().trim().toUpperCase();

        if (!normalizedExistingIsbn.equals(normalizedNewIsbn)) {
            if (bookRepository.existsByIsbn(bookDTO.isbn())) {
                log.warn("Attempted to update book with duplicate ISBN: {}", bookDTO.isbn());
                throw new DuplicateResourceException("ISBN", bookDTO.isbn());
            }
        }

        // Update fields using MapStruct
        bookMapper.updateEntityFromDTO(bookDTO, existingBook);
        Book updatedBook = bookRepository.save(existingBook);

        log.info("Book updated successfully: {}", updatedBook.getTitle());
        return bookMapper.toDTO(updatedBook);
    }

    /**
     * Delete a book by ID
     *
     * @param id Book ID to delete
     * @throws ResourceNotFoundException if book not found
     */
    public void deleteBook(Long id) {
        log.info("Deleting book with ID: {}", id);

        if (!bookRepository.existsById(id)) {
            log.error("Book not found with ID: {}", id);
            throw new ResourceNotFoundException("Book", id);
        }

        bookRepository.deleteById(id);
        log.info("Book deleted successfully with ID: {}", id);
    }

    /**
     * Get library statistics
     *
     * @return Map containing various statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getLibraryStats() {
        log.info("Calculating library statistics");

        Map<String, Object> stats = new HashMap<>();

        // Total books count
        long totalBooks = bookRepository.count();
        stats.put("totalBooks", totalBooks);

        // Unique authors
        List<String> uniqueAuthors = bookRepository.findAllUniqueAuthors();
        stats.put("uniqueAuthorsCount", uniqueAuthors.size());
        stats.put("uniqueAuthors", uniqueAuthors);

        // Books by year distribution
        List<Object[]> booksByYear = bookRepository.countBooksByYear();
        Map<Integer, Long> yearDistribution = new HashMap<>();
        for (Object[] row : booksByYear) {
            Integer year = (Integer) row[0];
            Long count = (Long) row[1];
            yearDistribution.put(year, count);
        }
        stats.put("booksByYear", yearDistribution);

        // Oldest book
        if (totalBooks > 0) {
            bookRepository.findTopByOrderByPublishedDateAsc().ifPresent(oldest ->
                    stats.put("oldestBook", Map.of(
                            "id", oldest.getId(),
                            "title", oldest.getTitle(),
                            "author", oldest.getAuthor(),
                            "publishedDate", oldest.getPublishedDate()
                    ))
            );

            // Newest book
            bookRepository.findTopByOrderByPublishedDateDesc().ifPresent(newest ->
                    stats.put("newestBook", Map.of(
                            "id", newest.getId(),
                            "title", newest.getTitle(),
                            "author", newest.getAuthor(),
                            "publishedDate", newest.getPublishedDate()
                    ))
            );
        }

        log.info("Statistics calculated: {} total books, {} unique authors", totalBooks, uniqueAuthors.size());
        return stats;
    }

    /**
     * Bulk import books from CSV file
     * Processes all rows and collects both successes and failures
     * <p>
     * CSV Format: title,author,isbn,publishedDate (YYYY-MM-DD)
     *
     * @param file CSV file containing book data
     * @return Map with success count, failure count, and error details
     * @throws FileProcessingException if file cannot be read
     */
    public Map<String, Object> bulkCreateBooks(MultipartFile file) {
        log.info("Starting bulk import from file: {}", file.getOriginalFilename());

        List<ImportError> errors = new ArrayList<>();
        int successCount = 0;
        int rowNumber;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVReader csvReader = new CSVReader(reader)) {

            // Read all lines
            List<String[]> rows = csvReader.readAll();

            if (rows.isEmpty()) {
                throw new FileProcessingException("CSV file is empty", file.getOriginalFilename());
            }

            // Skip header row if present (check if first row has "title" or looks like header)
            int startIndex = 0;
            if (rows.getFirst()[0].equalsIgnoreCase("title") || rows.getFirst()[0].equalsIgnoreCase("book")) {
                startIndex = 1;
                log.info("Detected header row, skipping it");
            }

            // Process each row
            for (int i = startIndex; i < rows.size(); i++) {
                rowNumber = i + 1; // Human-readable row number
                String[] row = rows.get(i);

                try {
                    // Validate row has correct number of columns
                    if (row.length < 4) {
                        errors.add(ImportError.builder()
                                .row(rowNumber)
                                .error("Invalid row format. Expected 4 columns: title,author,isbn,publishedDate")
                                .rawData(String.join(",", row))
                                .errorType("INVALID_FORMAT")
                                .build());
                        continue;
                    }

                    // Extract and trim data
                    String title = row[0].trim();
                    String author = row[1].trim();
                    String isbn = row[2].trim();
                    String publishedDateStr = row[3].trim();

                    // Validate non-empty fields
                    if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || publishedDateStr.isEmpty()) {
                        errors.add(ImportError.builder()
                                .row(rowNumber)
                                .title(title)
                                .isbn(isbn)
                                .error("All fields are required (title, author, isbn, publishedDate)")
                                .rawData(String.join(",", row))
                                .errorType("MISSING_FIELDS")
                                .build());
                        continue;
                    }

                    // Parse date
                    LocalDate publishedDate;
                    try {
                        publishedDate = LocalDate.parse(publishedDateStr, DATE_FORMATTER);
                    } catch (DateTimeParseException e) {
                        errors.add(ImportError.builder()
                                .row(rowNumber)
                                .title(title)
                                .isbn(isbn)
                                .error("Invalid date format. Use YYYY-MM-DD (e.g., 2024-01-15)")
                                .rawData(String.join(",", row))
                                .errorType("INVALID_DATE")
                                .build());
                        continue;
                    }

                    // Business validation: Future date check
                    try {
                        validatePublishedDate(publishedDate);
                    } catch (BusinessLogicException e) {
                        errors.add(ImportError.builder()
                                .row(rowNumber)
                                .title(title)
                                .isbn(isbn)
                                .error(e.getMessage())
                                .rawData(String.join(",", row))
                                .errorType("BUSINESS_LOGIC_ERROR")
                                .build());
                        continue;
                    }

                    // Check for duplicate ISBN
                    if (bookRepository.existsByIsbn(isbn)) {
                        errors.add(ImportError.builder()
                                .row(rowNumber)
                                .title(title)
                                .isbn(isbn)
                                .error("Book with this ISBN already exists")
                                .rawData(String.join(",", row))
                                .errorType("DUPLICATE_ISBN")
                                .build());
                        continue;
                    }

                    // Create and save book
                    Book book = Book.builder()
                            .title(title)
                            .author(author)
                            .isbn(isbn)
                            .publishedDate(publishedDate)
                            .build();

                    bookRepository.save(book);
                    successCount++;
                    log.debug("Successfully imported book: {} (row {})", title, rowNumber);

                } catch (Exception e) {
                    log.error("Unexpected error processing row {}: {}", rowNumber, e.getMessage());
                    errors.add(ImportError.builder()
                            .row(rowNumber)
                            .error("Unexpected error: " + e.getMessage())
                            .rawData(row.length > 0 ? String.join(",", row) : "")
                            .errorType("UNEXPECTED_ERROR")
                            .build());
                }
            }

            log.info("Bulk import completed: {} successful, {} failed out of {} total rows",
                    successCount, errors.size(), rows.size() - startIndex);

            return ImportResultFactory.createSuccessResult(successCount, errors.size(), errors);

        } catch (IOException e) {
            log.error("Failed to read CSV file: {}", e.getMessage());
            throw new FileProcessingException("Failed to read CSV file: " + e.getMessage(),
                    file.getOriginalFilename(), e);
        } catch (CsvException e) {
            log.error("Failed to parse CSV file: {}", e.getMessage());
            throw new FileProcessingException("Failed to parse CSV file: " + e.getMessage(),
                    file.getOriginalFilename(), e);
        }
    }


    /**
     * Bulk import books asynchronously
     * Returns immediately with job ID for status tracking
     *
     * @param file CSV file
     * @return Job ID for tracking progress
     */
    public String bulkCreateBooksAsync(MultipartFile file) {
        log.info("Starting async bulk import from file: {}", file.getOriginalFilename());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> rows = csvReader.readAll();

            int startIndex = 0;
            if (!rows.isEmpty() && rows.getFirst()[0].equalsIgnoreCase("title")) {
                startIndex = 1;
            }

            int totalRows = rows.size() - startIndex;
            String jobId = importJobTracker.createJob(totalRows);

            processImportAsync(jobId, rows, startIndex);

            return jobId;

        } catch (IOException | CsvException e) {
            log.error("Failed to read CSV file: {}", e.getMessage());
            throw new FileProcessingException("Failed to read CSV file: " + e.getMessage(),
                    file.getOriginalFilename(), e);
        }
    }

    /**
     * Async processing method - runs in background thread
     */
    @Async("bulkImportExecutor")
    protected void processImportAsync(String jobId, List<String[]> rows, int startIndex) {
        log.info("Processing import job {} asynchronously", jobId);

        ImportJob job = importJobTracker.getJob(jobId).orElseThrow();
        job.setStatus(ImportJob.ImportStatus.PROCESSING);
        job.setStartTime(LocalDateTime.now());
        importJobTracker.updateJob(job);

        List<ImportError> errors = new ArrayList<>();
        int successCount = 0;

        try {
            for (int i = startIndex; i < rows.size(); i++) {
                int rowNumber = i + 1;
                String[] row = rows.get(i);

                try {
                    if (row.length < 4) {
                        errors.add(ImportError.builder()
                                .row(rowNumber)
                                .error("Invalid row format")
                                .rawData(String.join(",", row))
                                .errorType("INVALID_FORMAT")
                                .build());
                        continue;
                    }

                    String title = row[0].trim();
                    String author = row[1].trim();
                    String isbn = row[2].trim();
                    String publishedDateStr = row[3].trim();

                    if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || publishedDateStr.isEmpty()) {
                        errors.add(ImportError.builder()
                                .row(rowNumber)
                                .title(title)
                                .isbn(isbn)
                                .error("All fields are required")
                                .rawData(String.join(",", row))
                                .errorType("MISSING_FIELDS")
                                .build());
                        continue;
                    }

                    LocalDate publishedDate;
                    try {
                        publishedDate = LocalDate.parse(publishedDateStr, DATE_FORMATTER);
                    } catch (DateTimeParseException e) {
                        errors.add(ImportError.builder()
                                .row(rowNumber)
                                .title(title)
                                .isbn(isbn)
                                .error("Invalid date format. Use YYYY-MM-DD")
                                .rawData(String.join(",", row))
                                .errorType("INVALID_DATE")
                                .build());
                        continue;
                    }

                    try {
                        validatePublishedDate(publishedDate);
                    } catch (BusinessLogicException e) {
                        errors.add(ImportError.builder()
                                .row(rowNumber)
                                .title(title)
                                .isbn(isbn)
                                .error(e.getMessage())
                                .rawData(String.join(",", row))
                                .errorType("BUSINESS_LOGIC_ERROR")
                                .build());
                        continue;
                    }

                    if (bookRepository.existsByIsbn(isbn)) {
                        errors.add(ImportError.builder()
                                .row(rowNumber)
                                .title(title)
                                .isbn(isbn)
                                .error("Book with this ISBN already exists")
                                .rawData(String.join(",", row))
                                .errorType("DUPLICATE_ISBN")
                                .build());
                        continue;
                    }

                    Book book = Book.builder()
                            .title(title)
                            .author(author)
                            .isbn(isbn)
                            .publishedDate(publishedDate)
                            .build();

                    bookRepository.save(book);
                    successCount++;

                } catch (Exception e) {
                    log.error("Unexpected error processing row {}: {}", rowNumber, e.getMessage());
                    errors.add(ImportError.builder()
                            .row(rowNumber)
                            .error("Unexpected error: " + e.getMessage())
                            .rawData(row.length > 0 ? String.join(",", row) : "")
                            .errorType("UNEXPECTED_ERROR")
                            .build());
                }

                // Update progress
                job.setProcessedRows(i - startIndex + 1);
                job.setSuccessCount(successCount);
                job.setFailureCount(errors.size());
                importJobTracker.updateJob(job);

                // small delay to avoid overwhelming DB
                if (i % 100 == 0) {
                    Thread.sleep(10);
                }
            }

            job.setStatus(ImportJob.ImportStatus.COMPLETED);
            job.setEndTime(LocalDateTime.now());
            job.setErrors(errors);
            importJobTracker.updateJob(job);

            log.info("Import job {} completed: {} successful, {} failed",
                    jobId, successCount, errors.size());

        } catch (Exception e) {
            log.error("Import job {} failed: {}", jobId, e.getMessage());
            job.setStatus(ImportJob.ImportStatus.FAILED);
            job.setEndTime(LocalDateTime.now());
            importJobTracker.updateJob(job);
        }
    }

    /**
     * Business validation: Ensure published date is not in the future
     *
     * @param publishedDate Date to validate
     * @throws BusinessLogicException if date is in the future
     */
    private void validatePublishedDate(LocalDate publishedDate) {
        if (publishedDate.isAfter(LocalDate.now())) {
            throw new BusinessLogicException(
                    "Published date cannot be in the future",
                    Map.of(
                            "providedDate", publishedDate.toString(),
                            "currentDate", LocalDate.now().toString()
                    )
            );
        }
    }
}

