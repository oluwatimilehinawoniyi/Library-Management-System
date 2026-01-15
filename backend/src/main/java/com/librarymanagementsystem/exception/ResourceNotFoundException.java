package com.librarymanagementsystem.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends LibraryManagementException {
    private final Long resourceId;
    private final String resourceType;

    public ResourceNotFoundException(String resourceType, Long id) {
        super(String.format("%s with ID %d not found", resourceType, id), "RESOURCE_NOT_FOUND");
        this.resourceId = id;
        this.resourceType = resourceType;
    }

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
        this.resourceId = null;
        this.resourceType = null;
    }
}
