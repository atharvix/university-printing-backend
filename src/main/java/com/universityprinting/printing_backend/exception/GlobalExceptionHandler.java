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
