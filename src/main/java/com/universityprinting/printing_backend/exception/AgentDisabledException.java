package com.universityprinting.printing_backend.exception;

public class AgentDisabledException extends RuntimeException {
    public AgentDisabledException(String message) {
        super(message);
    }
}
