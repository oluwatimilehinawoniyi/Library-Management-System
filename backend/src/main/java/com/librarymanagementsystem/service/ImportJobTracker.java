package com.librarymanagementsystem.service;

import com.librarymanagementsystem.model.ImportJob;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory storage for import job tracking (this could have been in Redis/DB in prod)
 * Thread-safe using ConcurrentHashMap
 */
@Component
public class ImportJobTracker {

    private final Map<String, ImportJob> jobs = new ConcurrentHashMap<>();

    /**
     * Create a new import job with unique ID
     */
    public String createJob(int totalRows) {
        String jobId = UUID.randomUUID().toString();
        ImportJob job = ImportJob.builder()
                .jobId(jobId)
                .status(ImportJob.ImportStatus.PENDING)
                .totalRows(totalRows)
                .processedRows(0)
                .successCount(0)
                .failureCount(0)
                .build();
        jobs.put(jobId, job);
        return jobId;
    }

    /**
     * Get job by ID
     */
    public Optional<ImportJob> getJob(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    /**
     * Update job
     */
    public void updateJob(ImportJob job) {
        jobs.put(job.getJobId(), job);
    }
}
