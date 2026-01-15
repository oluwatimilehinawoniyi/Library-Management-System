package com.lms.controller;

import com.lms.model.ApiResponse;
import com.lms.model.BookDTO;
import com.lms.model.PageResponse;
import com.lms.service.BookService;
import com.lms.util.AlertUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MainController {

    // Service
    private final BookService bookService = new BookService();

    // Pagination state
    private int currentPage = 0;
    private int pageSize = 10;
    private int totalPages = 0;
    private String sortBy = "id";
    private String sortDirection = "ASC";
    private String searchKeyword = null;

    // Table and columns
    @FXML
    private TableView<BookDTO> booksTable;
    @FXML
    private TableColumn<BookDTO, Long> idColumn;
    @FXML
    private TableColumn<BookDTO, String> titleColumn;
    @FXML
    private TableColumn<BookDTO, String> authorColumn;
    @FXML
    private TableColumn<BookDTO, String> isbnColumn;
    @FXML
    private TableColumn<BookDTO, LocalDate> publishedDateColumn;

    // Form fields
    @FXML
    private TextField idField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField authorField;
    @FXML
    private TextField isbnField;
    @FXML
    private DatePicker publishedDatePicker;

    // Buttons
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button importButton;

    // Search
    @FXML
    private TextField searchField;

    // Pagination controls
    @FXML
    private Button firstPageButton;
    @FXML
    private Button prevPageButton;
    @FXML
    private Button nextPageButton;
    @FXML
    private Button lastPageButton;
    @FXML
    private ComboBox<Integer> pageSizeComboBox;
    @FXML
    private Label pageInfoLabel;
    @FXML
    private Label recordCountLabel;

    // Status labels
//    @FXML
//    private Label statusLabel;
    @FXML
    private Label connectionStatusLabel;
    @FXML
    private Label lastActionLabel;

    private boolean isEditMode = false;
    private Long selectedBookId = null;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupTableSelectionListener();
        setupPageSizeComboBox();
        checkBackendConnection();
        loadBooks();
        updateStatusBar("Application started", true);
    }

    /**
     * Setup table columns with property bindings
     */
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        publishedDateColumn.setCellValueFactory(new PropertyValueFactory<>("publishedDate"));

        // Make table sortable
        booksTable.setOnSort(event -> {
            TableColumn<BookDTO, ?> sortColumn = booksTable.getSortOrder().isEmpty()
                    ? null : booksTable.getSortOrder().getFirst();

            if (sortColumn != null) {
                sortBy = getSortFieldName(sortColumn);
                sortDirection = sortColumn.getSortType() == TableColumn.SortType.ASCENDING ? "ASC" : "DESC";
                loadBooks();
            }
        });
    }

    /**
     * Get field name for sorting from column
     */
    private String getSortFieldName(TableColumn<BookDTO, ?> column) {
        if (column == idColumn) return "id";
        if (column == titleColumn) return "title";
        if (column == authorColumn) return "author";
        if (column == isbnColumn) return "isbn";
        if (column == publishedDateColumn) return "publishedDate";
        return "id";
    }

    /**
     * Setup table row selection listener
     */
    private void setupTableSelectionListener() {
        booksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFormWithBook(newSelection);
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
                isEditMode = true;
                selectedBookId = newSelection.getId();
                saveButton.setText("Save Changes");
            } else {
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
    }

    /**
     * Setup page size combo box
     */
    private void setupPageSizeComboBox() {
        pageSizeComboBox.setItems(FXCollections.observableArrayList(5, 10, 20, 50, 100));
        pageSizeComboBox.setValue(pageSize);
    }

    private void loadBooks() {
        new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis();
                ApiResponse<PageResponse<BookDTO>> response;

                if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                    response = bookService.searchBooks(searchKeyword, currentPage, pageSize);
                } else {
                    response = bookService.getAllBooks(currentPage, pageSize, sortBy, sortDirection);
                }

                long responseTime = System.currentTimeMillis() - startTime;

                if (response.isSuccess()) {
                    PageResponse<BookDTO> page = response.getData();

                    Platform.runLater(() -> {
                        booksTable.setItems(FXCollections.observableArrayList(page.getContent()));
                        totalPages = page.getTotalPages();
                        updatePaginationControls(page);
                        updateStatusBar("Books loaded successfully", true);

                        if (responseTime > 3000) {
                            setConnectionStatus("SLOW");
                        } else {
                            setConnectionStatus("CONNECTED");
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        AlertUtils.showError("Load Error", response.getErrorMessage());
                        updateStatusBar("Failed to load books", false);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setConnectionStatus("DISCONNECTED");
                    AlertUtils.showError("Connection Error",
                            "Failed to connect to backend: " + e.getMessage() +
                                    "\n\nMake sure the Spring Boot backend is running on localhost:8080");
                    updateStatusBar("Backend connection failed", false);
                    connectionStatusLabel.setText("● Backend: Disconnected");
                    connectionStatusLabel.setStyle("-fx-text-fill: #e74c3c;");
                });
            }
        }).start();
    }

    /**
     * Update pagination controls based on page response
     */
    private void updatePaginationControls(PageResponse<BookDTO> page) {
        firstPageButton.setDisable(page.isFirst());
        prevPageButton.setDisable(!page.hasPrevious());
        nextPageButton.setDisable(!page.hasNext());
        lastPageButton.setDisable(page.isLast());

        pageInfoLabel.setText(String.format("Page %d of %d", currentPage + 1, totalPages));
        recordCountLabel.setText(page.getPaginationInfo());
    }

    /**
     * Populate form fields with selected book data
     */
    private void populateFormWithBook(BookDTO book) {
        idField.setText(book.getId().toString());
        titleField.setText(book.getTitle());
        authorField.setText(book.getAuthor());
        isbnField.setText(book.getIsbn());
        publishedDatePicker.setValue(book.getPublishedDate());
    }

    /**
     * Clear all form fields
     */
    private void clearForm() {
        idField.clear();
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        publishedDatePicker.setValue(null);
        booksTable.getSelectionModel().clearSelection();
        isEditMode = false;
        selectedBookId = null;
        saveButton.setText("Create Book");
    }

    /**
     * Build BookDTO from form fields
     */
    private BookDTO buildBookFromForm() {
        return BookDTO.builder()
                .id(selectedBookId)
                .title(titleField.getText().trim())
                .author(authorField.getText().trim())
                .isbn(isbnField.getText().trim())
                .publishedDate(publishedDatePicker.getValue())
                .build();
    }

    /**
     * Validate form inputs
     */
    private boolean validateForm() {
        if (titleField.getText().trim().isEmpty()) {
            AlertUtils.showWarning("Validation Error", "Please enter a book title");
            titleField.requestFocus();
            return false;
        }
        if (authorField.getText().trim().isEmpty()) {
            AlertUtils.showWarning("Validation Error", "Please enter an author name");
            authorField.requestFocus();
            return false;
        }
        if (isbnField.getText().trim().isEmpty()) {
            AlertUtils.showWarning("Validation Error", "Please enter an ISBN");
            isbnField.requestFocus();
            return false;
        }
        if (publishedDatePicker.getValue() == null) {
            AlertUtils.showWarning("Validation Error", "Please select a published date");
            publishedDatePicker.requestFocus();
            return false;
        }
        if (publishedDatePicker.getValue().isAfter(LocalDate.now())) {
            AlertUtils.showWarning("Validation Error", "Published date cannot be in the future");
            publishedDatePicker.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Update status bar
     */
    private void updateStatusBar(String action, boolean success) {
        lastActionLabel.setText("Last Action: " + action);
        if (success) {
            connectionStatusLabel.setText("● Backend: Connected");
            connectionStatusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        }
    }

    private void checkBackendConnection() {
        new Thread(() -> {
            try {
                bookService.getAllBooks(0, 1, "id", "ASC");
                Platform.runLater(() -> setConnectionStatus("CONNECTED"));
            } catch (Exception e) {
                Platform.runLater(() -> setConnectionStatus("DISCONNECTED"));
            }
        }).start();
    }

    private void setConnectionStatus(String status) {
        switch (status) {
            case "CONNECTED":
                connectionStatusLabel.setText("● Connected");
                connectionStatusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                break;
            case "SLOW":
                connectionStatusLabel.setText("● Slow Connection");
                connectionStatusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                break;
            case "DISCONNECTED":
                connectionStatusLabel.setText("● Disconnected");
                connectionStatusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                break;
        }
    }


    // ==================== Event Handlers ====================

    @FXML
    private void handleAdd() {
        clearForm();
        isEditMode = false;
        saveButton.setText("Create Book");
        titleField.requestFocus();
    }

    @FXML
    private void handleUpdate() {
        if (selectedBookId == null) {
            AlertUtils.showWarning("No Selection", "Please select a book to update");
            return;
        }
        isEditMode = true;
        titleField.requestFocus();
    }

    @FXML
    private void handleDelete() {
        BookDTO selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("No Selection", "Please select a book to delete");
            return;
        }

        Optional<ButtonType> result = AlertUtils.showConfirmation(
                "Delete Book",
                "Are you sure you want to delete this book?",
                String.format("Title: %s\nAuthor: %s\nISBN: %s",
                        selected.getTitle(), selected.getAuthor(), selected.getIsbn())
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    bookService.deleteBook(selected.getId());

                    Platform.runLater(() -> {
                        AlertUtils.showSuccess("Success", "Book deleted successfully");
                        clearForm();
                        loadBooks();
                        updateStatusBar("Book deleted: " + selected.getTitle(), true);
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        AlertUtils.showError("Delete Error", "Failed to delete book: " + e.getMessage());
                        updateStatusBar("Delete failed", false);
                    });
                }
            }).start();
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        BookDTO book = buildBookFromForm();

        new Thread(() -> {
            try {
                if (isEditMode && selectedBookId != null) {
                    ApiResponse<BookDTO> response = bookService.updateBook(selectedBookId, book);

                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            AlertUtils.showSuccess("Success", "Book updated successfully");
                            clearForm();
                            loadBooks();
                            updateStatusBar("Book updated: " + book.getTitle(), true);
                        } else {
                            AlertUtils.showError("Update Error", response.getErrorMessage());
                            updateStatusBar("Update failed", false);
                        }
                    });
                } else {
                    ApiResponse<BookDTO> response = bookService.createBook(book);

                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            AlertUtils.showSuccess("Success", "Book added successfully");
                            clearForm();
                            loadBooks();
                            updateStatusBar("Book created: " + book.getTitle(), true);
                        } else {
                            AlertUtils.showError("Create Error", response.getErrorMessage());
                            updateStatusBar("Create failed", false);
                        }
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    AlertUtils.showError("Save Error", "Failed to save book: " + e.getMessage());
                    updateStatusBar("Save failed", false);
                });
            }
        }).start();
    }

    @FXML
    private void handleRefresh() {
        clearForm();
        loadBooks();
        updateStatusBar("Data refreshed", true);
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    @FXML
    private void handleSearch() {
        searchKeyword = searchField.getText().trim();
        if (searchKeyword.isEmpty()) {
            AlertUtils.showWarning("Search", "Please enter a search keyword");
            return;
        }
        currentPage = 0;
        loadBooks();
        updateStatusBar("Searching for: " + searchKeyword, true);
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        searchKeyword = null;
        currentPage = 0;
        loadBooks();
        updateStatusBar("Search cleared", true);
    }

    @FXML
    private void handleFirstPage() {
        currentPage = 0;
        loadBooks();
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            loadBooks();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadBooks();
        }
    }

    @FXML
    private void handleLastPage() {
        currentPage = totalPages - 1;
        loadBooks();
    }

    @FXML
    private void handlePageSizeChange() {
        Integer newSize = pageSizeComboBox.getValue();
        if (newSize != null && newSize != pageSize) {
            pageSize = newSize;
            currentPage = 0;
            loadBooks();
        }
    }

    @FXML
    private void handleImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File to Import");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showOpenDialog(importButton.getScene().getWindow());

        if (file != null) {
            new Thread(() -> {
                try {
                    ApiResponse<Map<String, Object>> response = bookService.bulkImportBooks(file);

                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            Map<String, Object> result = response.getData();

                            if (result.containsKey("async") && (Boolean) result.get("async")) {
                                String jobId = (String) result.get("jobId");
                                AlertUtils.showInfo("Large File Import",
                                        "File is large - import running in background.\n\n" +
                                                "Job ID: " + jobId + "\n\n" +
                                                "Refresh the table in a few moments to see imported books.");
                            } else {
                                int success = ((Number) result.get("successCount")).intValue();
                                int failed = ((Number) result.get("failureCount")).intValue();

                                StringBuilder message = new StringBuilder();
                                message.append(String.format("Import completed!\n\nSuccessful: %d\nFailed: %d", success, failed));

                                if (failed > 0 && result.containsKey("errors")) {
                                    @SuppressWarnings("unchecked")
                                    List<Map<String, Object>> errors = (List<Map<String, Object>>) result.get("errors");
                                    message.append("\n\n─── Failed Records ───\n");

                                    int showMax = Math.min(errors.size(), 10); // only show max 10 errors
                                    for (int i = 0; i < showMax; i++) {
                                        Map<String, Object> error = errors.get(i);
                                        message.append(String.format("\nRow %d: %s",
                                                ((Number) error.get("row")).intValue(),
                                                error.get("error")));

                                        if (error.containsKey("title") && error.get("title") != null) {
                                            message.append(String.format("\n  Book: %s", error.get("title")));
                                        }
                                    }

                                    if (errors.size() > 10) {
                                        message.append(String.format("\n\n... and %d more errors", errors.size() - 10));
                                    }
                                }

                                if (failed > 0) {
                                    AlertUtils.showWarning("Import Completed with Errors", message.toString());
                                } else {
                                    AlertUtils.showSuccess("Import Successful", message.toString());
                                }
                            }

                            loadBooks();
                            updateStatusBar("CSV import completed", true);
                        } else {
                            AlertUtils.showError("Import Error", response.getErrorMessage());
                            updateStatusBar("Import failed", false);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        AlertUtils.showError("Import Error", "Failed to import CSV: " + e.getMessage());
                        updateStatusBar("Import failed", false);
                    });
                }
            }).start();
        }
    }

    @FXML
    private void handleStats() {
        new Thread(() -> {
            try {
                ApiResponse<Map<String, Object>> response = bookService.getStatistics();

                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        Map<String, Object> stats = response.getData();

                        StringBuilder message = new StringBuilder();
                        message.append(String.format("Total Books: %s\n", stats.get("totalBooks")));
                        message.append(String.format("Unique Authors: %s\n\n", stats.get("uniqueAuthorsCount")));

                        if (stats.containsKey("oldestBook")) {
                            Map<String, Object> oldest = (Map<String, Object>) stats.get("oldestBook");
                            message.append(String.format("Oldest Book: %s (%s)\n",
                                    oldest.get("title"), oldest.get("publishedDate")));
                        }

                        if (stats.containsKey("newestBook")) {
                            Map<String, Object> newest = (Map<String, Object>) stats.get("newestBook");
                            message.append(String.format("Newest Book: %s (%s)",
                                    newest.get("title"), newest.get("publishedDate")));
                        }

                        AlertUtils.showInfo("Library Statistics", message.toString());
                        updateStatusBar("Statistics displayed", true);
                    } else {
                        AlertUtils.showError("Statistics Error", response.getErrorMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    AlertUtils.showError("Statistics Error", "Failed to fetch statistics: " + e.getMessage());
                });
            }
        }).start();
    }
}
