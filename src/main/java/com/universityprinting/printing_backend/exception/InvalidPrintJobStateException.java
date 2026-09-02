package com.universityprinting.printing_backend.exception;

public class InvalidPrintJobStateException extends RuntimeException {
    public InvalidPrintJobStateException(String message) {
        super(message);
    }
}
