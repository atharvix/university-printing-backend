package com.universityprinting.printing_backend.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationFailed(AuthenticationFailedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "error", "Authentication failed",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "error", "Conflict",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<Map<String, String>> handleInvalidFile(InvalidFileException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "error", "Invalid file",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleDocumentNotFound(DocumentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "error", "Not found",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(UnauthorizedDocumentAccessException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedDocumentAccess(UnauthorizedDocumentAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "error", "Forbidden",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePaymentNotFound(PaymentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "error", "Not found",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(AgentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAgentNotFound(AgentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "error", "Not found",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(AgentDisabledException.class)
    public ResponseEntity<Map<String, String>> handleAgentDisabled(AgentDisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "error", "Forbidden",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(UnauthorizedAgentAccessException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedAgentAccess(UnauthorizedAgentAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "error", "Forbidden",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(PrinterNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePrinterNotFound(PrinterNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "error", "Not found",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(PrinterUnavailableException.class)
    public ResponseEntity<Map<String, String>> handlePrinterUnavailable(PrinterUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "error", "Printer unavailable",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(IncompatiblePrinterException.class)
    public ResponseEntity<Map<String, String>> handleIncompatiblePrinter(IncompatiblePrinterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "error", "Incompatible printer",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(JobAlreadyClaimedException.class)
    public ResponseEntity<Map<String, String>> handleJobAlreadyClaimed(JobAlreadyClaimedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "error", "Conflict",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(PaymentVerificationException.class)
    public ResponseEntity<Map<String, String>> handlePaymentVerificationFailed(PaymentVerificationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "error", "Payment verification failed",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(DuplicatePaymentException.class)
    public ResponseEntity<Map<String, String>> handleDuplicatePayment(DuplicatePaymentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "error", "Conflict",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(UnauthorizedPaymentAccessException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedPaymentAccess(UnauthorizedPaymentAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "error", "Forbidden",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<Map<String, String>> handleStorageException(StorageException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "error", "Storage error",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(PrintJobNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePrintJobNotFound(PrintJobNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "error", "Not found",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(InvalidPrintJobStateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPrintJobState(InvalidPrintJobStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "error", "Conflict",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(UnauthorizedPrintJobAccessException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedPrintJobAccess(UnauthorizedPrintJobAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "error", "Forbidden",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxUploadSizeExceeded(org.springframework.web.multipart.MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of(
            "error", "Payload Too Large",
            "message", "File size exceeds the maximum limit of 10MB"
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Validation failed");
        response.put("details", errors);
        return response;
    }
}
