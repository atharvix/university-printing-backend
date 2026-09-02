package com.universityprinting.printing_backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PrintJobTest {

    @Test
    void defaultConstructor_SetsStatusToQueued() {
        PrintJob job = new PrintJob();
        assertEquals(PrintJobStatus.QUEUED, job.getStatus());
    }

    @Test
    void parameterizedConstructor_ValidFields_SetsAllValues() {
        Instant now = Instant.now();
        BigDecimal price = new BigDecimal("15.50");
        PrintJob job = new PrintJob(
            "job-001",
            "owner-123",
            "doc-456",
            3,
            ColorMode.COLOR,
            PaperSize.A4,
            true,
            10,
            price,
            PrintJobStatus.PROCESSING,
            now,
            now
        );

        assertEquals("job-001", job.getId());
        assertEquals("owner-123", job.getOwnerId());
        assertEquals("doc-456", job.getDocumentId());
        assertEquals(3, job.getCopies());
        assertEquals(ColorMode.COLOR, job.getColorMode());
        assertEquals(PaperSize.A4, job.getPaperSize());
        assertTrue(job.getDuplex());
        assertEquals(10, job.getPageCount());
        assertEquals(new BigDecimal("15.50"), job.getPrice());
        assertEquals(PrintJobStatus.PROCESSING, job.getStatus());
        assertEquals(now, job.getCreatedAt());
        assertEquals(now, job.getUpdatedAt());
    }

    @Test
    void constructorAndSetter_NullStatus_DefaultsToQueued() {
        PrintJob job = new PrintJob(
            "job-001",
            "owner-123",
            "doc-456",
            1,
            ColorMode.BLACK_WHITE,
            PaperSize.A4,
            false,
            5,
            new BigDecimal("5.00"),
            null,
            Instant.now(),
            Instant.now()
        );
        assertEquals(PrintJobStatus.QUEUED, job.getStatus());

        job.setStatus(null);
        assertEquals(PrintJobStatus.QUEUED, job.getStatus());
    }

    @Test
    void colorModeEnum_ContainsExpectedValues() {
        assertEquals(2, ColorMode.values().length);
        assertEquals(ColorMode.BLACK_WHITE, ColorMode.valueOf("BLACK_WHITE"));
        assertEquals(ColorMode.COLOR, ColorMode.valueOf("COLOR"));
    }

    @Test
    void paperSizeEnum_ContainsExpectedValues() {
        assertEquals(2, PaperSize.values().length);
        assertEquals(PaperSize.A4, PaperSize.valueOf("A4"));
        assertEquals(PaperSize.A3, PaperSize.valueOf("A3"));
    }

    @Test
    void printJobStatusEnum_ContainsExpectedValues() {
        assertEquals(6, PrintJobStatus.values().length);
        assertEquals(PrintJobStatus.QUEUED, PrintJobStatus.valueOf("QUEUED"));
        assertEquals(PrintJobStatus.PROCESSING, PrintJobStatus.valueOf("PROCESSING"));
        assertEquals(PrintJobStatus.PRINTING, PrintJobStatus.valueOf("PRINTING"));
        assertEquals(PrintJobStatus.COMPLETED, PrintJobStatus.valueOf("COMPLETED"));
        assertEquals(PrintJobStatus.FAILED, PrintJobStatus.valueOf("FAILED"));
        assertEquals(PrintJobStatus.CANCELLED, PrintJobStatus.valueOf("CANCELLED"));
    }
}
