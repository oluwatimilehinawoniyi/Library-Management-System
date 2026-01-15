package com.librarymanagementsystem.commons.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;


public record BookDTO(
        @Schema(description = "Unique identifier of the book", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @NotBlank(message = "Title is required")
        @Schema(description = "Title of the book", example = "Law Report", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @NotBlank(message = "Author is required")
        @Schema(description = "Author of the book", example = "Renaissance Law Publishers", requiredMode = Schema.RequiredMode.REQUIRED)
        String author,

        @NotBlank(message = "ISBN is required")
        @Pattern(regexp = "^[0-9X-]{10,17}$", message = "ISBN must be 10-17 characters (numbers, X, or hyphens)")
        @Schema(description = "International Standard Book Number", example = "978-0-13-235088-4", requiredMode = Schema.RequiredMode.REQUIRED)
        String isbn,

        @NotNull(message = "Published date is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "Publication date", example = "2008-08-01", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate publishedDate
) {
    public BookDTO {
        if (title != null) {
            title = title.trim();
        }
        if (author != null) {
            author = author.trim();
        }
        if (isbn != null) {
            isbn = isbn.trim().toUpperCase();
        }
    }
}
