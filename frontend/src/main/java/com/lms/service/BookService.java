package com.lms.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lms.model.ApiResponse;
import com.lms.model.BookDTO;
import com.lms.model.PageResponse;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class BookService {

    private static final String BASE_URL = "http://localhost:8080/api/v1/books";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final long ASYNC_THRESHOLD_KB = 50; // configurable, so for testing bulk upload, it could be reduced or so

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BookService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * get all books with pagination and sorting
     */
    public ApiResponse<PageResponse<BookDTO>> getAllBooks(int page, int size, String sortBy, String direction)
            throws Exception {
        String url = String.format("%s?page=%d&size=%d&sortBy=%s&direction=%s",
                BASE_URL, page, size, sortBy, direction);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            TypeReference<ApiResponse<PageResponse<BookDTO>>> typeRef =
                    new TypeReference<ApiResponse<PageResponse<BookDTO>>>() {
                    };
            return objectMapper.readValue(response.body(), typeRef);
        } else {
            throw new RuntimeException("Failed to fetch books: " + response.statusCode());
        }
    }

    /**
     * search books by keyword with pagination
     */
    public ApiResponse<PageResponse<BookDTO>> searchBooks(String keyword, int page, int size)
            throws Exception {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = String.format("%s/search?keyword=%s&page=%d&size=%d",
                BASE_URL, encodedKeyword, page, size);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            TypeReference<ApiResponse<PageResponse<BookDTO>>> typeRef =
                    new TypeReference<ApiResponse<PageResponse<BookDTO>>>() {
                    };
            return objectMapper.readValue(response.body(), typeRef);
        } else {
            throw new RuntimeException("Search failed: " + response.statusCode());
        }
    }

    /**
     * get a single book by ID
     */
    public ApiResponse<BookDTO> getBookById(Long id) throws Exception {
        String url = BASE_URL + "/" + id;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        TypeReference<ApiResponse<BookDTO>> typeRef = new TypeReference<ApiResponse<BookDTO>>() {
        };
        return objectMapper.readValue(response.body(), typeRef);
    }

    /**
     * Create a new book
     */
    public ApiResponse<BookDTO> createBook(BookDTO book) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(book);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return getBookDTOApiResponse(request);
    }

    /**
     * Update an existing book
     */
    public ApiResponse<BookDTO> updateBook(Long id, BookDTO book) throws Exception {
        String url = BASE_URL + "/" + id;
        String jsonBody = objectMapper.writeValueAsString(book);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return getBookDTOApiResponse(request);
    }

    /**
     * Delete a book
     */
    public void deleteBook(Long id) throws Exception {
        String url = BASE_URL + "/" + id;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new RuntimeException("Failed to delete book: " + response.statusCode());
        }
    }

    /**
     * Bulk import books from CSV file
     */
    public ApiResponse<Map<String, Object>> bulkImportBooks(File csvFile) throws Exception {

        long fileSizeKB = csvFile.length() / 1024;

        // if it's a large file - use async
        if (fileSizeKB > ASYNC_THRESHOLD_KB) {
            return bulkImportBooksAsync(csvFile);
        }

        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        byte[] fileBytes = Files.readAllBytes(csvFile.toPath());

        // Build multipart form data
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("--").append(boundary).append("\r\n");
        bodyBuilder.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                .append(csvFile.getName()).append("\"\r\n");
        bodyBuilder.append("Content-Type: text/csv\r\n\r\n");

        String header = bodyBuilder.toString();
        String footer = "\r\n--" + boundary + "--\r\n";

        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
        byte[] footerBytes = footer.getBytes(StandardCharsets.UTF_8);

        byte[] body = new byte[headerBytes.length + fileBytes.length + footerBytes.length];
        System.arraycopy(headerBytes, 0, body, 0, headerBytes.length);
        System.arraycopy(fileBytes, 0, body, headerBytes.length, fileBytes.length);
        System.arraycopy(footerBytes, 0, body, headerBytes.length + fileBytes.length, footerBytes.length);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/bulk"))
                .timeout(Duration.ofMinutes(5))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        TypeReference<ApiResponse<Map<String, Object>>> typeRef =
                new TypeReference<ApiResponse<Map<String, Object>>>() {
                };
        ApiResponse<Map<String, Object>> apiResponse = objectMapper.readValue(response.body(), typeRef);

        if (response.statusCode() >= 400) {
            throw new RuntimeException(apiResponse.getErrorMessage());
        }

        return apiResponse;
    }

    /**
     * Get library statistics
     */
    public ApiResponse<Map<String, Object>> getStatistics() throws Exception {
        String url = BASE_URL + "/stats";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        TypeReference<ApiResponse<Map<String, Object>>> typeRef =
                new TypeReference<ApiResponse<Map<String, Object>>>() {
                };
        return objectMapper.readValue(response.body(), typeRef);
    }

    private ApiResponse<BookDTO> getBookDTOApiResponse(HttpRequest request) throws Exception, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        TypeReference<ApiResponse<BookDTO>> typeRef = new TypeReference<ApiResponse<BookDTO>>() {
        };
        ApiResponse<BookDTO> apiResponse = objectMapper.readValue(response.body(), typeRef);

        if (response.statusCode() >= 400) {
            String errorMsg = apiResponse.getMessage();

            if (apiResponse.getError() != null && apiResponse.getError().getDetails() != null) {
                Object details = apiResponse.getError().getDetails();
                if (details instanceof Map) {
                    Map<String, String> validationErrors = (Map<String, String>) details;
                    StringBuilder sb = new StringBuilder(errorMsg).append("\n\n");
                    validationErrors.forEach((field, message) ->
                            sb.append("• ").append(field).append(": ").append(message).append("\n")
                    );
                    errorMsg = sb.toString().trim();
                }
            }

            throw new RuntimeException(errorMsg);
        }

        return apiResponse;
    }


    private ApiResponse<Map<String, Object>> bulkImportBooksAsync(File csvFile) throws Exception {
        String url = BASE_URL + "/bulk-async";

        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        byte[] fileBytes = Files.readAllBytes(csvFile.toPath());

        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("--").append(boundary).append("\r\n");
        bodyBuilder.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                .append(csvFile.getName()).append("\"\r\n");
        bodyBuilder.append("Content-Type: text/csv\r\n\r\n");

        String header = bodyBuilder.toString();
        String footer = "\r\n--" + boundary + "--\r\n";

        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
        byte[] footerBytes = footer.getBytes(StandardCharsets.UTF_8);

        byte[] body = new byte[headerBytes.length + fileBytes.length + footerBytes.length];
        System.arraycopy(headerBytes, 0, body, 0, headerBytes.length);
        System.arraycopy(fileBytes, 0, body, headerBytes.length, fileBytes.length);
        System.arraycopy(footerBytes, 0, body, headerBytes.length + fileBytes.length, footerBytes.length);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(5))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        TypeReference<ApiResponse<Map<String, String>>> typeRef =
                new TypeReference<ApiResponse<Map<String, String>>>() {
                };
        ApiResponse<Map<String, String>> asyncResponse = objectMapper.readValue(response.body(), typeRef);


        if (response.statusCode() >= 400 || !asyncResponse.isSuccess()) {
            throw new RuntimeException("Async import failed: " + asyncResponse.getErrorMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("async", true);
        result.put("jobId", asyncResponse.getData().get("jobId"));
        result.put("message", "Large file - import running in background");

        return ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Import started asynchronously")
                .data(result)
                .build();
    }
}