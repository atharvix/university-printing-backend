package com.universityprinting.printing_backend.service;

import com.universityprinting.printing_backend.exception.InvalidFileException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileValidator {

    public static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB
    private static final byte[] PDF_MAGIC_BYTES = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    public void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Uploaded file is empty or missing");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("File size exceeds the 10MB limit");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new InvalidFileException("File name is missing");
        }

        // Prevent path traversal
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            // Check if path traversal is attempted
            String sanitized = Paths.get(originalFilename).getFileName().toString();
            if (sanitized.contains("..") || sanitized.trim().isEmpty()) {
                throw new InvalidFileException("Invalid or dangerous file name");
            }
        }

        // Validate PDF signature / magic bytes (first 5 bytes must equal "%PDF-")
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = new byte[PDF_MAGIC_BYTES.length];
            int bytesRead = inputStream.read(header);

            if (bytesRead < PDF_MAGIC_BYTES.length) {
                throw new InvalidFileException("File is not a valid PDF document (header too short)");
            }

            for (int i = 0; i < PDF_MAGIC_BYTES.length; i++) {
                if (header[i] != PDF_MAGIC_BYTES[i]) {
                    throw new InvalidFileException("File content does not match standard PDF magic bytes (%PDF-)");
                }
            }
        } catch (IOException e) {
            throw new InvalidFileException("Failed to read uploaded file content");
        }
    }

    public String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return "document.pdf";
        }
        String fileNameOnly = Paths.get(originalFilename).getFileName().toString().trim();
        // Remove special characters that could cause issues, keep letters, numbers, dots, dashes, underscores
        String cleaned = fileNameOnly.replaceAll("[^a-zA-Z0-9._-]", "_");
        return cleaned.isEmpty() ? "document.pdf" : cleaned;
    }
}
