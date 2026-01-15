package com.librarymanagementsystem.model;

import com.librarymanagementsystem.commons.dto.ImportError;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tracks the status of an async bulk import job
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportJob {
    private String jobId;
    private ImportStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalRows;
    private int processedRows;
    private int successCount;
    private int failureCount;
    private List<ImportError> errors;

    public enum ImportStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    public double getProgress() {
        if (totalRows == 0) return 0.0;
        return (processedRows * 100.0) / totalRows;
    }
}
