package com.universityprinting.printing_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.universityprinting.printing_backend.dto.PrintJobEventResponse;
import com.universityprinting.printing_backend.dto.PrintJobResponse;
import com.universityprinting.printing_backend.exception.InvalidPrintJobStateException;
import com.universityprinting.printing_backend.exception.JobAlreadyClaimedException;
import com.universityprinting.printing_backend.exception.PrintJobNotFoundException;
import com.universityprinting.printing_backend.model.ActorType;
import com.universityprinting.printing_backend.model.ColorMode;
import com.universityprinting.printing_backend.model.PaperSize;
import com.universityprinting.printing_backend.model.PrintJob;
import com.universityprinting.printing_backend.model.PrintJobEvent;
import com.universityprinting.printing_backend.model.PrintJobStatus;
import com.universityprinting.printing_backend.model.Printer;
import com.universityprinting.printing_backend.model.PrinterStatus;
import com.universityprinting.printing_backend.repository.PrintJobEventRepository;
import com.universityprinting.printing_backend.repository.PrintJobRepository;
import com.universityprinting.printing_backend.repository.PrinterRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@ExtendWith(MockitoExtension.class)
class QueueServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private PrintJobRepository printJobRepository;

    @Mock
    private PrinterRepository printerRepository;

    @Mock
    private PrintJobEventRepository printJobEventRepository;

    @Mock
    private PrinterService printerService;

    private QueueService queueService;

    @BeforeEach
    void setUp() {
        queueService = new QueueService(
            mongoTemplate,
            printJobRepository,
            printerRepository,
            printJobEventRepository,
            printerService
        );
    }

    @Test
    void claimNextJobForPrinter_Success() {
        Printer printer = new Printer("ptr-1", "HP Laser", "Library", PrinterStatus.ONLINE, Set.of(ColorMode.BLACK_WHITE), Set.of(PaperSize.A4), false, "agent-1", true, null, Instant.now(), Instant.now());
        when(printerRepository.findById("ptr-1")).thenReturn(Optional.of(printer));

        PrintJob claimed = new PrintJob("job-1", "student-1", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5, new BigDecimal("10.00"), PrintJobStatus.PROCESSING, true, "ptr-1", "agent-1", Instant.now(), Instant.now());
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(PrintJob.class)))
            .thenReturn(claimed);

        PrintJobResponse response = queueService.claimNextJobForPrinter("ptr-1", "agent-1");

        assertNotNull(response);
        assertEquals("job-1", response.getId());
        assertEquals(PrintJobStatus.PROCESSING, response.getStatus());
        assertEquals("ptr-1", response.getAssignedPrinterId());
        assertEquals("agent-1", response.getAssignedAgentId());
        verify(printJobEventRepository).save(any(PrintJobEvent.class));
    }

    @Test
    void claimNextJobForPrinter_NoEligibleJob_ReturnsNull() {
        Printer printer = new Printer("ptr-1", "HP Laser", "Library", PrinterStatus.ONLINE, Set.of(ColorMode.BLACK_WHITE), Set.of(PaperSize.A4), false, "agent-1", true, null, Instant.now(), Instant.now());
        when(printerRepository.findById("ptr-1")).thenReturn(Optional.of(printer));
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(PrintJob.class)))
            .thenReturn(null);

        PrintJobResponse response = queueService.claimNextJobForPrinter("ptr-1", "agent-1");

        assertNull(response);
    }

    @Test
    void claimSpecificJob_Success() {
        Printer printer = new Printer("ptr-1", "HP Laser", "Library", PrinterStatus.ONLINE, Set.of(ColorMode.BLACK_WHITE), Set.of(PaperSize.A4), false, "agent-1", true, null, Instant.now(), Instant.now());
        PrintJob job = new PrintJob("job-1", "student-1", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5, new BigDecimal("10.00"), PrintJobStatus.QUEUED, true, null, null, Instant.now(), Instant.now());

        when(printerRepository.findById("ptr-1")).thenReturn(Optional.of(printer));
        when(printJobRepository.findById("job-1")).thenReturn(Optional.of(job));

        PrintJob claimed = new PrintJob("job-1", "student-1", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5, new BigDecimal("10.00"), PrintJobStatus.PROCESSING, true, "ptr-1", "agent-1", Instant.now(), Instant.now());
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(PrintJob.class)))
            .thenReturn(claimed);

        PrintJobResponse response = queueService.claimSpecificJob("job-1", "ptr-1", "agent-1");

        assertNotNull(response);
        assertEquals(PrintJobStatus.PROCESSING, response.getStatus());
        verify(printerService).validateCompatibility(printer, job);
        verify(printJobEventRepository).save(any(PrintJobEvent.class));
    }

    @Test
    void claimSpecificJob_AlreadyClaimed_ThrowsJobAlreadyClaimedException() {
        Printer printer = new Printer("ptr-1", "HP Laser", "Library", PrinterStatus.ONLINE, Set.of(ColorMode.BLACK_WHITE), Set.of(PaperSize.A4), false, "agent-1", true, null, Instant.now(), Instant.now());
        PrintJob job = new PrintJob("job-1", "student-1", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5, new BigDecimal("10.00"), PrintJobStatus.QUEUED, true, null, null, Instant.now(), Instant.now());

        when(printerRepository.findById("ptr-1")).thenReturn(Optional.of(printer));
        when(printJobRepository.findById("job-1")).thenReturn(Optional.of(job));
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(PrintJob.class)))
            .thenReturn(null);

        assertThrows(
            JobAlreadyClaimedException.class,
            () -> queueService.claimSpecificJob("job-1", "ptr-1", "agent-1")
        );
    }

    @Test
    void completeJob_Success() {
        PrintJob job = new PrintJob("job-1", "student-1", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5, new BigDecimal("10.00"), PrintJobStatus.PROCESSING, true, "ptr-1", "agent-1", Instant.now(), Instant.now());
        when(printJobRepository.findById("job-1")).thenReturn(Optional.of(job));
        when(printJobRepository.save(any(PrintJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PrintJobResponse response = queueService.completeJob("job-1", ActorType.OPERATOR, "op-1");

        assertEquals(PrintJobStatus.COMPLETED, response.getStatus());
        verify(printJobEventRepository).save(any(PrintJobEvent.class));
    }

    @Test
    void completeJob_InvalidState_ThrowsException() {
        PrintJob job = new PrintJob("job-1", "student-1", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5, new BigDecimal("10.00"), PrintJobStatus.QUEUED, true, null, null, Instant.now(), Instant.now());
        when(printJobRepository.findById("job-1")).thenReturn(Optional.of(job));

        assertThrows(
            InvalidPrintJobStateException.class,
            () -> queueService.completeJob("job-1", ActorType.OPERATOR, "op-1")
        );
    }

    @Test
    void failJob_Success() {
        PrintJob job = new PrintJob("job-1", "student-1", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5, new BigDecimal("10.00"), PrintJobStatus.PROCESSING, true, "ptr-1", "agent-1", Instant.now(), Instant.now());
        when(printJobRepository.findById("job-1")).thenReturn(Optional.of(job));
        when(printJobRepository.save(any(PrintJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PrintJobResponse response = queueService.failJob("job-1", "Paper jam", ActorType.AGENT, "agent-1");

        assertEquals(PrintJobStatus.FAILED, response.getStatus());
        verify(printJobEventRepository).save(any(PrintJobEvent.class));
    }

    @Test
    void getJobEvents_Success() {
        PrintJobEvent event = new PrintJobEvent("evt-1", "job-1", ActorType.AGENT, "agent-1", "ASSIGNED", PrintJobStatus.QUEUED, PrintJobStatus.PROCESSING, "Assigned", Instant.now());
        when(printJobEventRepository.findByPrintJobIdOrderByCreatedAtAsc("job-1")).thenReturn(List.of(event));

        List<PrintJobEventResponse> responses = queueService.getJobEvents("job-1");

        assertEquals(1, responses.size());
        assertEquals("evt-1", responses.get(0).getId());
        assertEquals("ASSIGNED", responses.get(0).getEventType());
    }
}
