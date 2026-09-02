package com.universityprinting.printing_backend.exception;

public class IncompatiblePrinterException extends RuntimeException {
    public IncompatiblePrinterException(String message) {
        super(message);
    }
}
