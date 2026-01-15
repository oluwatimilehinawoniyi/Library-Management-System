package com.librarymanagementsystem.commons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an error that occurred during CSV bulk import.
 * Contains details about which row failed and why.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportError {
    private int row;
    private String isbn;
    private String title;
    private String error;
    private String rawData;
    private String errorType;
}
