package com.universityprinting.printing_backend.exception;

public class UnauthorizedPrintJobAccessException extends RuntimeException {
    public UnauthorizedPrintJobAccessException(String message) {
        super(message);
    }
}
