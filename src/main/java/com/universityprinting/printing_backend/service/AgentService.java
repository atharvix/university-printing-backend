package com.universityprinting.printing_backend.service;

import com.universityprinting.printing_backend.dto.AgentHeartbeatRequest;
import com.universityprinting.printing_backend.dto.AgentJobResponse;
import com.universityprinting.printing_backend.dto.AgentRegistrationResponse;
import com.universityprinting.printing_backend.dto.AgentResponse;
import com.universityprinting.printing_backend.dto.PrintJobResponse;
import com.universityprinting.printing_backend.dto.RegisterAgentRequest;
import com.universityprinting.printing_backend.exception.AgentDisabledException;
import com.universityprinting.printing_backend.exception.AgentNotFoundException;
import com.universityprinting.printing_backend.exception.DocumentNotFoundException;
import com.universityprinting.printing_backend.exception.PrintJobNotFoundException;
import com.universityprinting.printing_backend.exception.UnauthorizedAgentAccessException;
import com.universityprinting.printing_backend.model.ActorType;
import com.universityprinting.printing_backend.model.AgentStatus;
import com.universityprinting.printing_backend.model.Document;
import com.universityprinting.printing_backend.model.PrintAgent;
import com.universityprinting.printing_backend.model.PrintJob;
import com.universityprinting.printing_backend.model.PrintJobStatus;
import com.universityprinting.printing_backend.model.Printer;
import com.universityprinting.printing_backend.repository.DocumentRepository;
import com.universityprinting.printing_backend.repository.PrintAgentRepository;
import com.universityprinting.printing_backend.repository.PrintJobRepository;
import com.universityprinting.printing_backend.repository.PrinterRepository;
import com.universityprinting.printing_backend.security.AgentAuthenticationFilter;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    private final PrintAgentRepository printAgentRepository;
    private final PrinterRepository printerRepository;
    private final PrintJobRepository printJobRepository;
    private final DocumentRepository documentRepository;
    private final QueueService queueService;
    private final PrinterService printerService;
    private final StorageService storageService;

    public AgentService(
        PrintAgentRepository printAgentRepository,
        PrinterRepository printerRepository,
        PrintJobRepository printJobRepository,
        DocumentRepository documentRepository,
        QueueService queueService,
        PrinterService printerService,
        StorageService storageService
    ) {
        this.printAgentRepository = printAgentRepository;
        this.printerRepository = printerRepository;
        this.printJobRepository = printJobRepository;
        this.documentRepository = documentRepository;
        this.queueService = queueService;
        this.printerService = printerService;
        this.storageService = storageService;
    }

    public AgentRegistrationResponse registerAgent(RegisterAgentRequest request) {
        String rawApiKey = "agk_" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String apiKeyHash = AgentAuthenticationFilter.hashKey(rawApiKey);

        Instant now = Instant.now();
        PrintAgent agent = new PrintAgent(
            null,
            request.getName(),
            request.getHostName(),
            apiKeyHash,
            AgentStatus.ACTIVE,
            request.getAssignedPrinterIds() != null ? request.getAssignedPrinterIds() : new HashSet<>(),
            null,
            now,
            now
        );

        PrintAgent savedAgent = printAgentRepository.save(agent);

        if (request.getAssignedPrinterIds() != null) {
            for (String printerId : request.getAssignedPrinterIds()) {
                printerRepository.findById(printerId).ifPresent(p -> {
                    p.setAgentId(savedAgent.getId());
                    printerRepository.save(p);
                });
            }
        }

        log.info("[AGENT] Registered print agent {} ({})", savedAgent.getName(), savedAgent.getId());
        return AgentRegistrationResponse.of(savedAgent, rawApiKey);
    }

    public void heartbeat(String agentId, AgentHeartbeatRequest request) {
        PrintAgent agent = printAgentRepository.findById(agentId)
            .orElseThrow(() -> new AgentNotFoundException("Print agent not found with ID: " + agentId));

        if (agent.getStatus() == AgentStatus.DISABLED) {
            throw new AgentDisabledException("Print agent " + agentId + " is disabled");
        }

        agent.setLastHeartbeatAt(Instant.now());
        if (request != null && request.getStatus() != null) {
            agent.setStatus(request.getStatus());
        }
        printAgentRepository.save(agent);

        if (request != null && request.getPrinterStatuses() != null) {
            request.getPrinterStatuses().forEach((printerId, status) -> {
                try {
                    printerService.updatePrinterStatus(printerId, status);
                    printerService.recordHeartbeat(printerId);
                } catch (Exception e) {
                    log.warn("[AGENT] Failed to update printer {} status during heartbeat: {}", printerId, e.getMessage());
                }
            });
        }
    }

    public AgentJobResponse pollNextJob(String agentId) {
        PrintAgent agent = printAgentRepository.findById(agentId)
            .orElseThrow(() -> new AgentNotFoundException("Print agent not found with ID: " + agentId));

        if (agent.getStatus() == AgentStatus.DISABLED) {
            throw new AgentDisabledException("Print agent " + agentId + " is disabled");
        }

        Set<String> printerIds = new HashSet<>(agent.getAssignedPrinterIds());
        List<Printer> mappedPrinters = printerRepository.findByAgentId(agentId);
        for (Printer p : mappedPrinters) {
            printerIds.add(p.getId());
        }

        for (String printerId : printerIds) {
            PrintJobResponse claimed = queueService.claimNextJobForPrinter(printerId, agentId);
            if (claimed != null) {
                PrintJob job = printJobRepository.findById(claimed.getId()).orElse(null);
                if (job != null) {
                    Document document = documentRepository.findById(job.getDocumentId())
                        .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + job.getDocumentId()));

                    String downloadUrl = storageService.generateDownloadUrl(document.getStorageKey(), document.getResourceType());
                    log.info("[AGENT] Dispatched job {} to agent {} for printer {}", job.getId(), agentId, printerId);
                    return AgentJobResponse.from(job, downloadUrl);
                }
            }
        }

        return null;
    }

    public PrintJobResponse acknowledgeJob(String agentId, String jobId) {
        PrintJob job = printJobRepository.findById(jobId)
            .orElseThrow(() -> new PrintJobNotFoundException("Print job not found with ID: " + jobId));

        if (!agentId.equals(job.getAssignedAgentId())) {
            throw new UnauthorizedAgentAccessException("Print job " + jobId + " is not assigned to agent " + agentId);
        }

        PrintJobStatus prev = job.getStatus();
        job.setStatus(PrintJobStatus.PRINTING);
        job.setUpdatedAt(Instant.now());
        PrintJob saved = printJobRepository.save(job);

        queueService.recordEvent(
            jobId,
            ActorType.AGENT,
            agentId,
            "PRINTING",
            prev,
            PrintJobStatus.PRINTING,
            "Agent " + agentId + " started physical printing"
        );

        log.info("[AGENT] Job {} acknowledged and marked PRINTING by agent {}", jobId, agentId);
        return PrintJobResponse.from(saved);
    }

    public PrintJobResponse completeJob(String agentId, String jobId) {
        PrintJob job = printJobRepository.findById(jobId)
            .orElseThrow(() -> new PrintJobNotFoundException("Print job not found with ID: " + jobId));

        if (!agentId.equals(job.getAssignedAgentId())) {
            throw new UnauthorizedAgentAccessException("Print job " + jobId + " is not assigned to agent " + agentId);
        }

        return queueService.completeJob(jobId, ActorType.AGENT, agentId);
    }

    public PrintJobResponse failJob(String agentId, String jobId, String reason) {
        PrintJob job = printJobRepository.findById(jobId)
            .orElseThrow(() -> new PrintJobNotFoundException("Print job not found with ID: " + jobId));

        if (!agentId.equals(job.getAssignedAgentId())) {
            throw new UnauthorizedAgentAccessException("Print job " + jobId + " is not assigned to agent " + agentId);
        }

        return queueService.failJob(jobId, reason, ActorType.AGENT, agentId);
    }

    public AgentResponse getAgentById(String id) {
        PrintAgent agent = printAgentRepository.findById(id)
            .orElseThrow(() -> new AgentNotFoundException("Print agent not found with ID: " + id));
        return AgentResponse.from(agent);
    }

    public List<AgentResponse> getAllAgents() {
        return printAgentRepository.findAll().stream()
            .map(AgentResponse::from)
            .toList();
    }
}
