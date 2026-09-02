package com.universityprinting.printing_backend.exception;

public class PrinterNotFoundException extends RuntimeException {
    public PrinterNotFoundException(String message) {
        super(message);
    }
}
