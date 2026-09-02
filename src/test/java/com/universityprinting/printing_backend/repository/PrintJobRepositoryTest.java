package com.universityprinting.printing_backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.universityprinting.printing_backend.model.ColorMode;
import com.universityprinting.printing_backend.model.PaperSize;
import com.universityprinting.printing_backend.model.PrintJob;
import com.universityprinting.printing_backend.model.PrintJobStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrintJobRepositoryTest {

    private PrintJobRepository printJobRepository;

    @BeforeEach
    void setUp() {
        printJobRepository = mock(PrintJobRepository.class);
    }

    @Test
    void findByOwnerId_ReturnsOnlyMatchingOwnerJobs() {
        PrintJob job1 = createSampleJob("job-1", "user-1", "doc-1", PrintJobStatus.QUEUED);
        PrintJob job2 = createSampleJob("job-2", "user-1", "doc-2", PrintJobStatus.COMPLETED);
        when(printJobRepository.findByOwnerId("user-1")).thenReturn(List.of(job1, job2));

        List<PrintJob> results = printJobRepository.findByOwnerId("user-1");

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(j -> "user-1".equals(j.getOwnerId())));
        verify(printJobRepository).findByOwnerId("user-1");
    }

    @Test
    void findByIdAndOwnerId_MatchingOwner_ReturnsJob() {
        PrintJob job = createSampleJob("job-1", "user-1", "doc-1", PrintJobStatus.QUEUED);
        when(printJobRepository.findByIdAndOwnerId("job-1", "user-1")).thenReturn(Optional.of(job));

        Optional<PrintJob> result = printJobRepository.findByIdAndOwnerId("job-1", "user-1");

        assertTrue(result.isPresent());
        assertEquals("job-1", result.get().getId());
        assertEquals("user-1", result.get().getOwnerId());
        verify(printJobRepository).findByIdAndOwnerId("job-1", "user-1");
    }

    @Test
    void findByIdAndOwnerId_DifferentOwner_ReturnsEmpty() {
        when(printJobRepository.findByIdAndOwnerId("job-1", "user-2")).thenReturn(Optional.empty());

        Optional<PrintJob> result = printJobRepository.findByIdAndOwnerId("job-1", "user-2");

        assertFalse(result.isPresent());
        verify(printJobRepository).findByIdAndOwnerId("job-1", "user-2");
    }

    @Test
    void findByDocumentId_ReturnsMatchingJobs() {
        PrintJob job = createSampleJob("job-1", "user-1", "doc-1", PrintJobStatus.QUEUED);
        when(printJobRepository.findByDocumentId("doc-1")).thenReturn(List.of(job));

        List<PrintJob> results = printJobRepository.findByDocumentId("doc-1");

        assertEquals(1, results.size());
        assertEquals("doc-1", results.get(0).getDocumentId());
        verify(printJobRepository).findByDocumentId("doc-1");
    }

    @Test
    void findByStatus_ReturnsMatchingJobs() {
        PrintJob job = createSampleJob("job-1", "user-1", "doc-1", PrintJobStatus.QUEUED);
        when(printJobRepository.findByStatus(PrintJobStatus.QUEUED)).thenReturn(List.of(job));

        List<PrintJob> results = printJobRepository.findByStatus(PrintJobStatus.QUEUED);

        assertEquals(1, results.size());
        assertEquals(PrintJobStatus.QUEUED, results.get(0).getStatus());
        verify(printJobRepository).findByStatus(PrintJobStatus.QUEUED);
    }

    private PrintJob createSampleJob(String id, String ownerId, String documentId, PrintJobStatus status) {
        return new PrintJob(
            id,
            ownerId,
            documentId,
            1,
            ColorMode.BLACK_WHITE,
            PaperSize.A4,
            false,
            5,
            new BigDecimal("5.00"),
            status,
            Instant.now(),
            Instant.now()
        );
    }
}
