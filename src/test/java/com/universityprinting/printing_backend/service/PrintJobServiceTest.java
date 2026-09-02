package com.universityprinting.printing_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.universityprinting.printing_backend.dto.CreatePrintJobRequest;
import com.universityprinting.printing_backend.dto.PrintJobResponse;
import com.universityprinting.printing_backend.exception.DocumentNotFoundException;
import com.universityprinting.printing_backend.exception.InvalidPrintJobStateException;
import com.universityprinting.printing_backend.exception.PrintJobNotFoundException;
import com.universityprinting.printing_backend.exception.UnauthorizedDocumentAccessException;
import com.universityprinting.printing_backend.exception.UnauthorizedPrintJobAccessException;
import com.universityprinting.printing_backend.model.ColorMode;
import com.universityprinting.printing_backend.model.Document;
import com.universityprinting.printing_backend.model.PaperSize;
import com.universityprinting.printing_backend.model.PrintJob;
import com.universityprinting.printing_backend.model.PrintJobStatus;
import com.universityprinting.printing_backend.repository.DocumentRepository;
import com.universityprinting.printing_backend.repository.PrintJobRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PrintJobServiceTest {

    @Mock
    private PrintJobRepository printJobRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private PricingService pricingService;

    private PrintJobService printJobService;

    @BeforeEach
    void setUp() {
        printJobService = new PrintJobService(printJobRepository, documentRepository, pricingService);
    }

    @Test
    void createPrintJob_Success() {
        CreatePrintJobRequest request = new CreatePrintJobRequest("doc-123", 2, ColorMode.COLOR, PaperSize.A4, true, 5);
        Document mockDoc = new Document("doc-123", "student-1", "sample.pdf", "k1", "raw", "application/pdf", 1024, Instant.now(), Instant.now());
        
        when(documentRepository.findById("doc-123")).thenReturn(Optional.of(mockDoc));
        when(pricingService.calculatePrice(ColorMode.COLOR, PaperSize.A4, true, 5, 2)).thenReturn(new BigDecimal("45.00"));
        when(printJobRepository.save(any(PrintJob.class))).thenAnswer(invocation -> {
            PrintJob job = invocation.getArgument(0);
            job.setId("job-999");
            return job;
        });

        PrintJobResponse response = printJobService.createPrintJob(request, "student-1");

        assertNotNull(response);
        assertEquals("job-999", response.getId());
        assertEquals("student-1", response.getOwnerId());
        assertEquals("doc-123", response.getDocumentId());
        assertEquals(2, response.getCopies());
        assertEquals(ColorMode.COLOR, response.getColorMode());
        assertEquals(PaperSize.A4, response.getPaperSize());
        assertEquals(true, response.getDuplex());
        assertEquals(5, response.getPageCount());
        assertEquals(new BigDecimal("45.00"), response.getPrice());
        assertEquals(PrintJobStatus.QUEUED, response.getStatus());
        verify(printJobRepository).save(any(PrintJob.class));
    }

    @Test
    void createPrintJob_DocumentNotFound_ThrowsDocumentNotFoundException() {
        CreatePrintJobRequest request = new CreatePrintJobRequest("doc-unknown", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 1);
        when(documentRepository.findById("doc-unknown")).thenReturn(Optional.empty());

        assertThrows(
            DocumentNotFoundException.class,
            () -> printJobService.createPrintJob(request, "student-1")
        );
    }

    @Test
    void createPrintJob_UnauthorizedDocumentOwner_ThrowsUnauthorizedDocumentAccessException() {
        CreatePrintJobRequest request = new CreatePrintJobRequest("doc-123", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 1);
        Document mockDoc = new Document("doc-123", "student-2", "sample.pdf", "k1", "raw", "application/pdf", 1024, Instant.now(), Instant.now());
        when(documentRepository.findById("doc-123")).thenReturn(Optional.of(mockDoc));

        assertThrows(
            UnauthorizedDocumentAccessException.class,
            () -> printJobService.createPrintJob(request, "student-1")
        );
    }

    @Test
    void getPrintJobsByOwner_ReturnsUserJobs() {
        PrintJob job1 = createSampleJob("j1", "student-1", "d1", PrintJobStatus.QUEUED);
        PrintJob job2 = createSampleJob("j2", "student-1", "d2", PrintJobStatus.COMPLETED);
        when(printJobRepository.findByOwnerId("student-1")).thenReturn(List.of(job1, job2));

        List<PrintJobResponse> result = printJobService.getPrintJobsByOwner("student-1");

        assertEquals(2, result.size());
        assertEquals("j1", result.get(0).getId());
        assertEquals("j2", result.get(1).getId());
    }

    @Test
    void getPrintJobByIdAndOwner_Success() {
        PrintJob job = createSampleJob("j1", "student-1", "d1", PrintJobStatus.QUEUED);
        when(printJobRepository.findById("j1")).thenReturn(Optional.of(job));

        PrintJobResponse response = printJobService.getPrintJobByIdAndOwner("j1", "student-1");

        assertNotNull(response);
        assertEquals("j1", response.getId());
        assertEquals("student-1", response.getOwnerId());
    }

    @Test
    void getPrintJobByIdAndOwner_NotFound_ThrowsPrintJobNotFoundException() {
        when(printJobRepository.findById("j-unknown")).thenReturn(Optional.empty());

        assertThrows(
            PrintJobNotFoundException.class,
            () -> printJobService.getPrintJobByIdAndOwner("j-unknown", "student-1")
        );
    }

    @Test
    void getPrintJobByIdAndOwner_DifferentOwner_ThrowsUnauthorizedPrintJobAccessException() {
        PrintJob job = createSampleJob("j1", "student-2", "d1", PrintJobStatus.QUEUED);
        when(printJobRepository.findById("j1")).thenReturn(Optional.of(job));

        assertThrows(
            UnauthorizedPrintJobAccessException.class,
            () -> printJobService.getPrintJobByIdAndOwner("j1", "student-1")
        );
    }

    @Test
    void cancelPrintJob_QueuedJob_SetsStatusToCancelled() {
        PrintJob job = createSampleJob("j1", "student-1", "d1", PrintJobStatus.QUEUED);
        when(printJobRepository.findById("j1")).thenReturn(Optional.of(job));
        when(printJobRepository.save(any(PrintJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PrintJobResponse response = printJobService.cancelPrintJob("j1", "student-1");

        assertNotNull(response);
        assertEquals(PrintJobStatus.CANCELLED, response.getStatus());
        verify(printJobRepository).save(job);
    }

    @Test
    void cancelPrintJob_ProcessingJob_ThrowsInvalidPrintJobStateException() {
        PrintJob job = createSampleJob("j1", "student-1", "d1", PrintJobStatus.PROCESSING);
        when(printJobRepository.findById("j1")).thenReturn(Optional.of(job));

        assertThrows(
            InvalidPrintJobStateException.class,
            () -> printJobService.cancelPrintJob("j1", "student-1")
        );
    }

    @Test
    void cancelPrintJob_PrintingJob_ThrowsInvalidPrintJobStateException() {
        PrintJob job = createSampleJob("j1", "student-1", "d1", PrintJobStatus.PRINTING);
        when(printJobRepository.findById("j1")).thenReturn(Optional.of(job));

        assertThrows(
            InvalidPrintJobStateException.class,
            () -> printJobService.cancelPrintJob("j1", "student-1")
        );
    }

    @Test
    void cancelPrintJob_CompletedJob_ThrowsInvalidPrintJobStateException() {
        PrintJob job = createSampleJob("j1", "student-1", "d1", PrintJobStatus.COMPLETED);
        when(printJobRepository.findById("j1")).thenReturn(Optional.of(job));

        assertThrows(
            InvalidPrintJobStateException.class,
            () -> printJobService.cancelPrintJob("j1", "student-1")
        );
    }

    @Test
    void cancelPrintJob_AlreadyCancelled_ThrowsInvalidPrintJobStateException() {
        PrintJob job = createSampleJob("j1", "student-1", "d1", PrintJobStatus.CANCELLED);
        when(printJobRepository.findById("j1")).thenReturn(Optional.of(job));

        assertThrows(
            InvalidPrintJobStateException.class,
            () -> printJobService.cancelPrintJob("j1", "student-1")
        );
    }

    @Test
    void cancelPrintJob_DifferentOwner_ThrowsUnauthorizedPrintJobAccessException() {
        PrintJob job = createSampleJob("j1", "student-2", "d1", PrintJobStatus.QUEUED);
        when(printJobRepository.findById("j1")).thenReturn(Optional.of(job));

        assertThrows(
            UnauthorizedPrintJobAccessException.class,
            () -> printJobService.cancelPrintJob("j1", "student-1")
        );
    }

    @Test
    void getPrintJobsByStatus_ReturnsMatchingJobs() {
        PrintJob job = createSampleJob("j1", "student-1", "d1", PrintJobStatus.QUEUED);
        when(printJobRepository.findByStatus(PrintJobStatus.QUEUED)).thenReturn(List.of(job));

        List<PrintJobResponse> result = printJobService.getPrintJobsByStatus(PrintJobStatus.QUEUED);

        assertEquals(1, result.size());
        assertEquals("j1", result.get(0).getId());
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
            new BigDecimal("10.00"),
            status,
            Instant.now(),
            Instant.now()
        );
    }
}
