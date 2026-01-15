package com.librarymanagementsystem.exception;

import lombok.Getter;

@Getter
public class BusinessLogicException extends LibraryManagementException {
    public BusinessLogicException(String message) {
        super(message, "BUSINESS_LOGIC_ERROR");
    }

    public BusinessLogicException(String message, Object details) {
        super(message, "BUSINESS_LOGIC_ERROR", details);
    }
}
