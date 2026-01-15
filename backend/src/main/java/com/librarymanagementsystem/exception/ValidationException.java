package com.librarymanagementsystem.exception;

import lombok.Getter;

@Getter
public class ValidationException extends LibraryManagementException {
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
    }

    public ValidationException(String message, Object details) {
        super(message, "VALIDATION_ERROR", details);
    }
}
