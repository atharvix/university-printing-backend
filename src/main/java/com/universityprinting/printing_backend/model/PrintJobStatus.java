package com.universityprinting.printing_backend.model;

public enum PrintJobStatus {
    QUEUED,
    PROCESSING,
    PRINTING,
    COMPLETED,
    FAILED,
    CANCELLED
}
