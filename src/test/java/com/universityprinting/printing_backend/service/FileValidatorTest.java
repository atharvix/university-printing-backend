package com.universityprinting.printing_backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.universityprinting.printing_backend.exception.InvalidFileException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class FileValidatorTest {

    private FileValidator fileValidator;

    @BeforeEach
    void setUp() {
        fileValidator = new FileValidator();
    }

    @Test
    void validatePdf_ValidPdf_DoesNotThrow() {
        byte[] validPdfBytes = "%PDF-1.4 test content".getBytes(StandardCharsets.US_ASCII);
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "assignment.pdf",
            "application/pdf",
            validPdfBytes
        );

        assertDoesNotThrow(() -> fileValidator.validatePdf(file));
    }

    @Test
    void validatePdf_EmptyFile_ThrowsInvalidFileException() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.pdf",
            "application/pdf",
            new byte[0]
        );

        InvalidFileException ex = assertThrows(
            InvalidFileException.class,
            () -> fileValidator.validatePdf(file)
        );
        assertEquals("Uploaded file is empty or missing", ex.getMessage());
    }

    @Test
    void validatePdf_NullFile_ThrowsInvalidFileException() {
        InvalidFileException ex = assertThrows(
            InvalidFileException.class,
            () -> fileValidator.validatePdf(null)
        );
        assertEquals("Uploaded file is empty or missing", ex.getMessage());
    }

    @Test
    void validatePdf_FileExceeds10MB_ThrowsInvalidFileException() {
        byte[] largeBytes = new byte[(int) FileValidator.MAX_FILE_SIZE_BYTES + 1];
        System.arraycopy("%PDF-".getBytes(StandardCharsets.US_ASCII), 0, largeBytes, 0, 5);

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "large.pdf",
            "application/pdf",
            largeBytes
        );

        InvalidFileException ex = assertThrows(
            InvalidFileException.class,
            () -> fileValidator.validatePdf(file)
        );
        assertEquals("File size exceeds the 10MB limit", ex.getMessage());
    }

    @Test
    void validatePdf_FakePdfWithWrongMagicBytes_ThrowsInvalidFileException() {
        // Content-Type is application/pdf, but actual bytes are not PDF
        byte[] fakePdfBytes = "NOT_A_PDF_CONTENT".getBytes(StandardCharsets.US_ASCII);
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "fake.pdf",
            "application/pdf",
            fakePdfBytes
        );

        InvalidFileException ex = assertThrows(
            InvalidFileException.class,
            () -> fileValidator.validatePdf(file)
        );
        assertEquals("File content does not match standard PDF magic bytes (%PDF-)", ex.getMessage());
    }

    @Test
    void validatePdf_ShortHeader_ThrowsInvalidFileException() {
        byte[] shortBytes = "%PD".getBytes(StandardCharsets.US_ASCII);
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "short.pdf",
            "application/pdf",
            shortBytes
        );

        InvalidFileException ex = assertThrows(
            InvalidFileException.class,
            () -> fileValidator.validatePdf(file)
        );
        assertEquals("File is not a valid PDF document (header too short)", ex.getMessage());
    }

    @Test
    void sanitizeFilename_PathTraversal_SanitizesProperly() {
        String dangerousFilename = "../../../etc/passwd.pdf";
        String sanitized = fileValidator.sanitizeFilename(dangerousFilename);
        assertEquals("passwd.pdf", sanitized);
    }

    @Test
    void sanitizeFilename_SpecialCharacters_Sanitized() {
        String filenameWithSpecialChars = "my proposal (v2) [final]!.pdf";
        String sanitized = fileValidator.sanitizeFilename(filenameWithSpecialChars);
        assertEquals("my_proposal__v2___final__.pdf", sanitized);
    }
}
