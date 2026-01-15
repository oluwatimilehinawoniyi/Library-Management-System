package com.librarymanagementsystem.exception;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends LibraryManagementException {
    private final String fieldName;
    private final Object fieldValue;

    public DuplicateResourceException(String fieldName, Object fieldValue) {
        super(String.format("%s '%s' already exists", fieldName, fieldValue), "DUPLICATE_RESOURCE");
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public DuplicateResourceException(String message, String fieldName, Object fieldValue) {
        super(message, "DUPLICATE_RESOURCE");
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
