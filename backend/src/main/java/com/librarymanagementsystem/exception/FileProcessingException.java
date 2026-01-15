package com.librarymanagementsystem.exception;

import lombok.Getter;

@Getter
public class FileProcessingException extends LibraryManagementException {
    private final String fileName;

    public FileProcessingException(String message, String fileName) {
        super(message, "FILE_PROCESSING_ERROR");
        this.fileName = fileName;
    }

    public FileProcessingException(String message, String fileName, Throwable cause) {
        super(message, "FILE_PROCESSING_ERROR", cause);
        this.fileName = fileName;
    }
}
