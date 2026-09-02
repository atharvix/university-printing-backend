package com.universityprinting.printing_backend.exception;

public class JobAlreadyClaimedException extends RuntimeException {
    public JobAlreadyClaimedException(String message) {
        super(message);
    }
}
