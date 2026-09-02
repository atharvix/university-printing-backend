package com.universityprinting.printing_backend.service;

import com.universityprinting.printing_backend.dto.CreatePrintJobRequest;
import com.universityprinting.printing_backend.dto.PrintJobResponse;
import com.universityprinting.printing_backend.exception.DocumentNotFoundException;
import com.universityprinting.printing_backend.exception.InvalidPrintJobStateException;
import com.universityprinting.printing_backend.exception.PrintJobNotFoundException;
import com.universityprinting.printing_backend.exception.UnauthorizedDocumentAccessException;
import com.universityprinting.printing_backend.exception.UnauthorizedPrintJobAccessException;
import com.universityprinting.printing_backend.model.Document;
import com.universityprinting.printing_backend.model.PrintJob;
import com.universityprinting.printing_backend.model.PrintJobStatus;
import com.universityprinting.printing_backend.repository.DocumentRepository;
import com.universityprinting.printing_backend.repository.PrintJobRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PrintJobService {

    private final PrintJobRepository printJobRepository;
    private final DocumentRepository documentRepository;
    private final PricingService pricingService;

    public PrintJobService(
        PrintJobRepository printJobRepository,
        DocumentRepository documentRepository,
        PricingService pricingService
    ) {
        this.printJobRepository = printJobRepository;
        this.documentRepository = documentRepository;
        this.pricingService = pricingService;
    }

    public PrintJobResponse createPrintJob(CreatePrintJobRequest request, String ownerId) {
        Document document = documentRepository.findById(request.getDocumentId())
            .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + request.getDocumentId()));

        if (!ownerId.equals(document.getOwnerId())) {
            throw new UnauthorizedDocumentAccessException("You do not have permission to create a print job for this document");
        }

        BigDecimal calculatedPrice = pricingService.calculatePrice(
            request.getColorMode(),
            request.getPaperSize(),
            Boolean.TRUE.equals(request.getDuplex()),
            request.getPageCount(),
            request.getCopies()
        );

        Instant now = Instant.now();
        PrintJob printJob = new PrintJob(
            null,
            ownerId,
            request.getDocumentId(),
            request.getCopies(),
            request.getColorMode(),
            request.getPaperSize(),
            request.getDuplex(),
            request.getPageCount(),
            calculatedPrice,
            PrintJobStatus.QUEUED,
            now,
            now
        );

        PrintJob savedJob = printJobRepository.save(printJob);
        return PrintJobResponse.from(savedJob);
    }

    public List<PrintJobResponse> getPrintJobsByOwner(String ownerId) {
        return printJobRepository.findByOwnerId(ownerId)
            .stream()
            .map(PrintJobResponse::from)
            .toList();
    }

    public PrintJobResponse getPrintJobByIdAndOwner(String id, String ownerId) {
        PrintJob printJob = printJobRepository.findById(id)
            .orElseThrow(() -> new PrintJobNotFoundException("Print job not found with ID: " + id));

        if (!ownerId.equals(printJob.getOwnerId())) {
            throw new UnauthorizedPrintJobAccessException("You do not have access to this print job");
        }

        return PrintJobResponse.from(printJob);
    }

    public PrintJobResponse cancelPrintJob(String id, String ownerId) {
        PrintJob printJob = printJobRepository.findById(id)
            .orElseThrow(() -> new PrintJobNotFoundException("Print job not found with ID: " + id));

        if (!ownerId.equals(printJob.getOwnerId())) {
            throw new UnauthorizedPrintJobAccessException("You do not have permission to cancel this print job");
        }

        if (printJob.getStatus() != PrintJobStatus.QUEUED) {
            throw new InvalidPrintJobStateException(
                "Cannot cancel print job in status: " + printJob.getStatus() + ". Only QUEUED jobs can be cancelled."
            );
        }

        printJob.setStatus(PrintJobStatus.CANCELLED);
        printJob.setUpdatedAt(Instant.now());

        PrintJob savedJob = printJobRepository.save(printJob);
        return PrintJobResponse.from(savedJob);
    }

    public List<PrintJobResponse> getPrintJobsByStatus(PrintJobStatus status) {
        return printJobRepository.findByStatus(status)
            .stream()
            .map(PrintJobResponse::from)
            .toList();
    }
}
