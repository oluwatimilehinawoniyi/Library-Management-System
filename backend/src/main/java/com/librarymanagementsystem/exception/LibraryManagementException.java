package com.librarymanagementsystem.exception;

import lombok.Getter;

@Getter
public class LibraryManagementException extends RuntimeException {
    private final String errorCode;
    private final Object details;

    public LibraryManagementException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    public LibraryManagementException(String message, String errorCode, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public LibraryManagementException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = null;
    }
}