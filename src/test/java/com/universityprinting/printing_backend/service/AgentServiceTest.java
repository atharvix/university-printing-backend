package com.universityprinting.printing_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.universityprinting.printing_backend.dto.AgentHeartbeatRequest;
import com.universityprinting.printing_backend.dto.AgentJobResponse;
import com.universityprinting.printing_backend.dto.AgentRegistrationResponse;
import com.universityprinting.printing_backend.dto.PrintJobResponse;
import com.universityprinting.printing_backend.dto.RegisterAgentRequest;
import com.universityprinting.printing_backend.exception.AgentDisabledException;
import com.universityprinting.printing_backend.exception.AgentNotFoundException;
import com.universityprinting.printing_backend.exception.UnauthorizedAgentAccessException;
import com.universityprinting.printing_backend.model.ActorType;
import com.universityprinting.printing_backend.model.AgentStatus;
import com.universityprinting.printing_backend.model.ColorMode;
import com.universityprinting.printing_backend.model.Document;
import com.universityprinting.printing_backend.model.PaperSize;
import com.universityprinting.printing_backend.model.PrintAgent;
import com.universityprinting.printing_backend.model.PrintJob;
import com.universityprinting.printing_backend.model.PrintJobStatus;
import com.universityprinting.printing_backend.model.Printer;
import com.universityprinting.printing_backend.model.PrinterStatus;
import com.universityprinting.printing_backend.repository.DocumentRepository;
import com.universityprinting.printing_backend.repository.PrintAgentRepository;
import com.universityprinting.printing_backend.repository.PrintJobRepository;
import com.universityprinting.printing_backend.repository.PrinterRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    @Mock
    private PrintAgentRepository printAgentRepository;

    @Mock
    private PrinterRepository printerRepository;

    @Mock
    private PrintJobRepository printJobRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private QueueService queueService;

    @Mock
    private PrinterService printerService;

    @Mock
    private StorageService storageService;

    private AgentService agentService;

    @BeforeEach
    void setUp() {
        agentService = new AgentService(
            printAgentRepository,
            printerRepository,
            printJobRepository,
            documentRepository,
            queueService,
            printerService,
            storageService
        );
    }

    @Test
    void registerAgent_Success() {
        RegisterAgentRequest request = new RegisterAgentRequest("Main-Lab-Agent", "agent-host-01", Set.of("ptr-1"));

        when(printAgentRepository.save(any(PrintAgent.class))).thenAnswer(invocation -> {
            PrintAgent a = invocation.getArgument(0);
            a.setId("agent-101");
            return a;
        });
        when(printerRepository.findById("ptr-1")).thenReturn(Optional.of(new Printer("ptr-1", "HP", "Lab", PrinterStatus.OFFLINE, Set.of(ColorMode.BLACK_WHITE), Set.of(PaperSize.A4), false, null, true, null, Instant.now(), Instant.now())));

        AgentRegistrationResponse response = agentService.registerAgent(request);

        assertNotNull(response);
        assertEquals("agent-101", response.getId());
        assertEquals("Main-Lab-Agent", response.getName());
        assertNotNull(response.getRawApiKey());
        assertTrue(response.getRawApiKey().startsWith("agk_"));
        verify(printAgentRepository).save(any(PrintAgent.class));
    }

    @Test
    void heartbeat_Success() {
        PrintAgent agent = new PrintAgent("agent-1", "Lab-Agent", "host-1", "hash", AgentStatus.ACTIVE, Set.of("ptr-1"), null, Instant.now(), Instant.now());
        when(printAgentRepository.findById("agent-1")).thenReturn(Optional.of(agent));

        AgentHeartbeatRequest request = new AgentHeartbeatRequest(AgentStatus.IDLE, Map.of("ptr-1", PrinterStatus.ONLINE));

        agentService.heartbeat("agent-1", request);

        assertEquals(AgentStatus.IDLE, agent.getStatus());
        assertNotNull(agent.getLastHeartbeatAt());
        verify(printAgentRepository).save(agent);
        verify(printerService).updatePrinterStatus("ptr-1", PrinterStatus.ONLINE);
        verify(printerService).recordHeartbeat("ptr-1");
    }

    @Test
    void heartbeat_DisabledAgent_ThrowsAgentDisabledException() {
        PrintAgent agent = new PrintAgent("agent-1", "Lab-Agent", "host-1", "hash", AgentStatus.DISABLED, Set.of("ptr-1"), null, Instant.now(), Instant.now());
        when(printAgentRepository.findById("agent-1")).thenReturn(Optional.of(agent));

        assertThrows(
            AgentDisabledException.class,
            () -> agentService.heartbeat("agent-1", new AgentHeartbeatRequest())
        );
    }

    @Test
    void pollNextJob_Success_ReturnsJobWithDownloadUrl() {
        PrintAgent agent = new PrintAgent("agent-1", "Lab-Agent", "host-1", "hash", AgentStatus.ACTIVE, Set.of("ptr-1"), null, Instant.now(), Instant.now());
        when(printAgentRepository.findById("agent-1")).thenReturn(Optional.of(agent));
        when(printerRepository.findByAgentId("agent-1")).thenReturn(List.of());

        PrintJobResponse claimed = new PrintJobResponse("job-1", "student-1", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5, new BigDecimal("10.00"), PrintJobStatus.PROCESSING, Instant.now(), Instant.now());
        when(queueService.claimNextJobForPrinter("ptr-1", "agent-1")).thenReturn(claimed);

        PrintJob job = new PrintJob("job-1", "student-1", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5, new BigDecimal("10.00"), PrintJobStatus.PROCESSING, true, "ptr-1", "agent-1", Instant.now(), Instant.now());
        when(printJobRepository.findById("job-1")).thenReturn(Optional.of(job));

        Document doc = new Document("doc-1", "student-1", "assignment.pdf", "key-1", "raw", "application/pdf", 1024L, Instant.now(), Instant.now());
        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));
        when(storageService.generateDownloadUrl("key-1", "raw")).thenReturn("https://storage.provider.com/doc-1.pdf");

        AgentJobResponse response = agentService.pollNextJob("agent-1");

        assertNotNull(response);
        assertEquals("job-1", response.getId());
        assertEquals("https://storage.provider.com/doc-1.pdf", response.getDocumentDownloadUrl());
        assertEquals("ptr-1", response.getAssignedPrinterId());
    }

    @Test
    void pollNextJob_NoJobs_ReturnsNull() {
        PrintAgent agent = new PrintAgent("agent-1", "Lab-Agent", "host-1", "hash", AgentStatus.ACTIVE, Set.of("ptr-1"), null, Instant.now(), Instant.now());
        when(printAgentRepository.findById("agent-1")).thenReturn(Optional.of(agent));
        when(printerRepository.findByAgentId("agent-1")).thenReturn(List.of());
        when(queueService.claimNextJobForPrinter("ptr-1", "agent-1")).thenReturn(null);

        AgentJobResponse response = agentService.pollNextJob("agent-1");

        assertNull(response);
    }

    @Test
    void acknowledgeJob_Success() {
        PrintJob job = new PrintJob("job-1", "student-1", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5, new BigDecimal("10.00"), PrintJobStatus.PROCESSING, true, "ptr-1", "agent-1", Instant.now(), Instant.now());
        when(printJobRepository.findById("job-1")).thenReturn(Optional.of(job));
        when(printJobRepository.save(any(PrintJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PrintJobResponse response = agentService.acknowledgeJob("agent-1", "job-1");

        assertEquals(PrintJobStatus.PRINTING, response.getStatus());
        verify(queueService).recordEvent("job-1", ActorType.AGENT, "agent-1", "PRINTING", PrintJobStatus.PROCESSING, PrintJobStatus.PRINTING, "Agent agent-1 started physical printing");
    }

    @Test
    void acknowledgeJob_UnauthorizedAgent_ThrowsException() {
        PrintJob job = new PrintJob("job-1", "student-1", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5, new BigDecimal("10.00"), PrintJobStatus.PROCESSING, true, "ptr-1", "agent-2", Instant.now(), Instant.now());
        when(printJobRepository.findById("job-1")).thenReturn(Optional.of(job));

        assertThrows(
            UnauthorizedAgentAccessException.class,
            () -> agentService.acknowledgeJob("agent-1", "job-1")
        );
    }

    @Test
    void completeJob_Success() {
        PrintJob job = new PrintJob("job-1", "student-1", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5, new BigDecimal("10.00"), PrintJobStatus.PRINTING, true, "ptr-1", "agent-1", Instant.now(), Instant.now());
        when(printJobRepository.findById("job-1")).thenReturn(Optional.of(job));

        agentService.completeJob("agent-1", "job-1");

        verify(queueService).completeJob("job-1", ActorType.AGENT, "agent-1");
    }

    @Test
    void failJob_Success() {
        PrintJob job = new PrintJob("job-1", "student-1", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5, new BigDecimal("10.00"), PrintJobStatus.PRINTING, true, "ptr-1", "agent-1", Instant.now(), Instant.now());
        when(printJobRepository.findById("job-1")).thenReturn(Optional.of(job));

        agentService.failJob("agent-1", "job-1", "Out of toner");

        verify(queueService).failJob("job-1", "Out of toner", ActorType.AGENT, "agent-1");
    }
}
