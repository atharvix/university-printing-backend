package com.universityprinting.printing_backend.service;

import com.universityprinting.printing_backend.dto.PrintJobEventResponse;
import com.universityprinting.printing_backend.dto.PrintJobResponse;
import com.universityprinting.printing_backend.exception.InvalidPrintJobStateException;
import com.universityprinting.printing_backend.exception.JobAlreadyClaimedException;
import com.universityprinting.printing_backend.exception.PrintJobNotFoundException;
import com.universityprinting.printing_backend.exception.PrinterNotFoundException;
import com.universityprinting.printing_backend.model.ActorType;
import com.universityprinting.printing_backend.model.PrintJob;
import com.universityprinting.printing_backend.model.PrintJobEvent;
import com.universityprinting.printing_backend.model.PrintJobStatus;
import com.universityprinting.printing_backend.model.Printer;
import com.universityprinting.printing_backend.repository.PrintJobEventRepository;
import com.universityprinting.printing_backend.repository.PrintJobRepository;
import com.universityprinting.printing_backend.repository.PrinterRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class QueueService {

    private static final Logger log = LoggerFactory.getLogger(QueueService.class);

    private final MongoTemplate mongoTemplate;
    private final PrintJobRepository printJobRepository;
    private final PrinterRepository printerRepository;
    private final PrintJobEventRepository printJobEventRepository;
    private final PrinterService printerService;

    public QueueService(
        MongoTemplate mongoTemplate,
        PrintJobRepository printJobRepository,
        PrinterRepository printerRepository,
        PrintJobEventRepository printJobEventRepository,
        PrinterService printerService
    ) {
        this.mongoTemplate = mongoTemplate;
        this.printJobRepository = printJobRepository;
        this.printerRepository = printerRepository;
        this.printJobEventRepository = printJobEventRepository;
        this.printerService = printerService;
    }

    public PrintJobResponse claimNextJobForPrinter(String printerId, String agentId) {
        Printer printer = printerRepository.findById(printerId)
            .orElseThrow(() -> new PrinterNotFoundException("Printer not found with ID: " + printerId));

        Criteria criteria = Criteria.where("status").is(PrintJobStatus.QUEUED)
            .and("queueEligible").is(true);

        if (printer.getSupportedColorModes() != null && !printer.getSupportedColorModes().isEmpty()) {
            criteria = criteria.and("colorMode").in(printer.getSupportedColorModes());
        }
        if (printer.getSupportedPaperSizes() != null && !printer.getSupportedPaperSizes().isEmpty()) {
            criteria = criteria.and("paperSize").in(printer.getSupportedPaperSizes());
        }
        if (!Boolean.TRUE.equals(printer.getDuplexSupported())) {
            criteria = criteria.and("duplex").is(false);
        }

        Query query = new Query(criteria).with(Sort.by(Sort.Direction.ASC, "createdAt"));
        Update update = new Update()
            .set("status", PrintJobStatus.PROCESSING)
            .set("assignedPrinterId", printer.getId())
            .set("assignedAgentId", agentId)
            .set("updatedAt", Instant.now());

        PrintJob claimed = mongoTemplate.findAndModify(
            query,
            update,
            FindAndModifyOptions.options().returnNew(true),
            PrintJob.class
        );

        if (claimed == null) {
            return null;
        }

        recordEvent(
            claimed.getId(),
            ActorType.AGENT,
            agentId,
            "ASSIGNED",
            PrintJobStatus.QUEUED,
            PrintJobStatus.PROCESSING,
            "Claimed by printer " + printer.getName() + " (" + printer.getId() + ")"
        );

        log.info("[QUEUE] Claimed next eligible job {} for printer {}", claimed.getId(), printer.getId());
        return PrintJobResponse.from(claimed);
    }

    public PrintJobResponse claimSpecificJob(String jobId, String printerId, String agentId) {
        Printer printer = printerRepository.findById(printerId)
            .orElseThrow(() -> new PrinterNotFoundException("Printer not found with ID: " + printerId));

        PrintJob printJob = printJobRepository.findById(jobId)
            .orElseThrow(() -> new PrintJobNotFoundException("Print job not found with ID: " + jobId));

        printerService.validateCompatibility(printer, printJob);

        Query query = new Query(
            Criteria.where("id").is(jobId)
                .and("status").is(PrintJobStatus.QUEUED)
                .and("queueEligible").is(true)
        );

        Update update = new Update()
            .set("status", PrintJobStatus.PROCESSING)
            .set("assignedPrinterId", printer.getId())
            .set("assignedAgentId", agentId)
            .set("updatedAt", Instant.now());

        PrintJob claimed = mongoTemplate.findAndModify(
            query,
            update,
            FindAndModifyOptions.options().returnNew(true),
            PrintJob.class
        );

        if (claimed == null) {
            throw new JobAlreadyClaimedException("Print job " + jobId + " is not queued/eligible or was already claimed");
        }

        recordEvent(
            claimed.getId(),
            agentId != null ? ActorType.AGENT : ActorType.OPERATOR,
            agentId != null ? agentId : "OPERATOR",
            "ASSIGNED",
            PrintJobStatus.QUEUED,
            PrintJobStatus.PROCESSING,
            "Claimed for printer " + printer.getName() + " (" + printer.getId() + ")"
        );

        log.info("[QUEUE] Claimed specific job {} for printer {}", jobId, printer.getId());
        return PrintJobResponse.from(claimed);
    }

    public PrintJobResponse completeJob(String jobId, ActorType actorType, String actorId) {
        PrintJob printJob = printJobRepository.findById(jobId)
            .orElseThrow(() -> new PrintJobNotFoundException("Print job not found with ID: " + jobId));

        if (printJob.getStatus() == PrintJobStatus.COMPLETED) {
            return PrintJobResponse.from(printJob);
        }

        if (printJob.getStatus() != PrintJobStatus.PROCESSING && printJob.getStatus() != PrintJobStatus.PRINTING) {
            throw new InvalidPrintJobStateException("Cannot complete job in status: " + printJob.getStatus());
        }

        PrintJobStatus prev = printJob.getStatus();
        printJob.setStatus(PrintJobStatus.COMPLETED);
        printJob.setUpdatedAt(Instant.now());
        PrintJob saved = printJobRepository.save(printJob);

        recordEvent(
            jobId,
            actorType,
            actorId,
            "COMPLETED",
            prev,
            PrintJobStatus.COMPLETED,
            "Print job completed successfully"
        );

        log.info("[QUEUE] Job {} completed by {} ({})", jobId, actorType, actorId);
        return PrintJobResponse.from(saved);
    }

    public PrintJobResponse failJob(String jobId, String reason, ActorType actorType, String actorId) {
        PrintJob printJob = printJobRepository.findById(jobId)
            .orElseThrow(() -> new PrintJobNotFoundException("Print job not found with ID: " + jobId));

        PrintJobStatus prev = printJob.getStatus();
        printJob.setStatus(PrintJobStatus.FAILED);
        printJob.setUpdatedAt(Instant.now());
        PrintJob saved = printJobRepository.save(printJob);

        recordEvent(
            jobId,
            actorType,
            actorId,
            "FAILED",
            prev,
            PrintJobStatus.FAILED,
            reason != null ? reason : "Print job failed"
        );

        log.info("[QUEUE] Job {} marked FAILED by {} ({}): {}", jobId, actorType, actorId, reason);
        return PrintJobResponse.from(saved);
    }

    public List<PrintJobResponse> getQueue(PrintJobStatus status) {
        if (status != null) {
            return printJobRepository.findByStatus(status).stream()
                .map(PrintJobResponse::from)
                .toList();
        }

        Query query = new Query(Criteria.where("queueEligible").is(true).and("status").is(PrintJobStatus.QUEUED))
            .with(Sort.by(Sort.Direction.ASC, "createdAt"));
        return mongoTemplate.find(query, PrintJob.class).stream()
            .map(PrintJobResponse::from)
            .toList();
    }

    public List<PrintJobEventResponse> getJobEvents(String jobId) {
        return printJobEventRepository.findByPrintJobIdOrderByCreatedAtAsc(jobId).stream()
            .map(PrintJobEventResponse::from)
            .toList();
    }

    public void recordEvent(
        String printJobId,
        ActorType actorType,
        String actorId,
        String eventType,
        PrintJobStatus previousStatus,
        PrintJobStatus newStatus,
        String message
    ) {
        PrintJobEvent event = new PrintJobEvent(
            null,
            printJobId,
            actorType,
            actorId,
            eventType,
            previousStatus,
            newStatus,
            message,
            Instant.now()
        );
        printJobEventRepository.save(event);
    }
}
