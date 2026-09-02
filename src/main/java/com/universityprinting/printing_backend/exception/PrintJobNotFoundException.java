package com.universityprinting.printing_backend.exception;

public class PrintJobNotFoundException extends RuntimeException {
    public PrintJobNotFoundException(String message) {
        super(message);
    }
}
