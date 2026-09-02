package com.universityprinting.printing_backend.exception;

public class PrinterUnavailableException extends RuntimeException {
    public PrinterUnavailableException(String message) {
        super(message);
    }
}
