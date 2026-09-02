package com.universityprinting.printing_backend.exception;

public class UnauthorizedDocumentAccessException extends RuntimeException {

    public UnauthorizedDocumentAccessException(String message) {
        super(message);
    }
}
