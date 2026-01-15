package com.librarymanagementsystem.service.factory;

import com.librarymanagementsystem.commons.dto.ImportError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory class for creating standardized bulk import result responses.
 * Implements Factory Pattern for consistent result creation.
 */
public class ImportResultFactory {

    /**
     * Create a success result for bulk import operation
     *
     * @param successCount Number of successfully imported books
     * @param failureCount Number of failed imports
     * @param errors       List of detailed error information
     * @return Map containing import results
     */
    public static Map<String, Object> createSuccessResult(
            int successCount,
            int failureCount,
            List<ImportError> errors) {

        Map<String, Object> result = new HashMap<>();

        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("totalProcessed", successCount + failureCount);
        result.put("errors", errors);

        // Create appropriate message based on results
        String message;
        if (failureCount == 0) {
            message = String.format("Successfully imported all %d books", successCount);
        } else if (successCount == 0) {
            message = String.format("Import failed: All %d books had errors", failureCount);
        } else {
            message = String.format("Partially successful: %d books imported, %d failed",
                    successCount, failureCount);
        }
        result.put("message", message);

        // Add error summary by type
        if (!errors.isEmpty()) {
            Map<String, Long> errorSummary = new HashMap<>();
            for (ImportError error : errors) {
                String type = error.getErrorType() != null ? error.getErrorType() : "UNKNOWN";
                errorSummary.put(type, errorSummary.getOrDefault(type, 0L) + 1);
            }
            result.put("errorSummary", errorSummary);
        }

        return result;
    }

    /**
     * Create a failure result when import cannot proceed
     * (e.g., file cannot be read, invalid format)
     *
     * @param reason Reason for failure
     * @return Map containing failure information
     */
    public static Map<String, Object> createFailureResult(String reason) {
        Map<String, Object> result = new HashMap<>();

        result.put("successCount", 0);
        result.put("failureCount", 0);
        result.put("totalProcessed", 0);
        result.put("errors", List.of());
        result.put("message", "Import failed: " + reason);

        return result;
    }

    /**
     * Create a result for empty file scenario
     *
     * @return Map containing empty file message
     */
    public static Map<String, Object> createEmptyFileResult() {
        return createFailureResult("CSV file is empty or contains no data rows");
    }
}