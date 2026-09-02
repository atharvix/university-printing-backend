package com.universityprinting.printing_backend.exception;

public class UnauthorizedAgentAccessException extends RuntimeException {
    public UnauthorizedAgentAccessException(String message) {
        super(message);
    }
}
