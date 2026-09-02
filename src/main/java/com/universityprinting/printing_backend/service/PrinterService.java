package com.universityprinting.printing_backend.service;

import com.universityprinting.printing_backend.dto.CreatePrinterRequest;
import com.universityprinting.printing_backend.dto.PrinterResponse;
import com.universityprinting.printing_backend.dto.UpdatePrinterRequest;
import com.universityprinting.printing_backend.exception.IncompatiblePrinterException;
import com.universityprinting.printing_backend.exception.PrinterNotFoundException;
import com.universityprinting.printing_backend.exception.PrinterUnavailableException;
import com.universityprinting.printing_backend.model.PrintJob;
import com.universityprinting.printing_backend.model.Printer;
import com.universityprinting.printing_backend.model.PrinterStatus;
import com.universityprinting.printing_backend.repository.PrinterRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PrinterService {

    private static final Logger log = LoggerFactory.getLogger(PrinterService.class);
    private final PrinterRepository printerRepository;

    public PrinterService(PrinterRepository printerRepository) {
        this.printerRepository = printerRepository;
    }

    public PrinterResponse createPrinter(CreatePrinterRequest request) {
        Instant now = Instant.now();
        Printer printer = new Printer(
            null,
            request.getName(),
            request.getLocation(),
            PrinterStatus.OFFLINE,
            request.getSupportedColorModes(),
            request.getSupportedPaperSizes(),
            request.getDuplexSupported(),
            request.getAgentId(),
            true,
            null,
            now,
            now
        );

        Printer saved = printerRepository.save(printer);
        log.info("[PRINTER] Created printer {} ({})", saved.getName(), saved.getId());
        return PrinterResponse.from(saved);
    }

    public PrinterResponse updatePrinter(String id, UpdatePrinterRequest request) {
        Printer printer = printerRepository.findById(id)
            .orElseThrow(() -> new PrinterNotFoundException("Printer not found with ID: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            printer.setName(request.getName());
        }
        if (request.getLocation() != null && !request.getLocation().isBlank()) {
            printer.setLocation(request.getLocation());
        }
        if (request.getStatus() != null) {
            printer.setStatus(request.getStatus());
        }
        if (request.getSupportedColorModes() != null && !request.getSupportedColorModes().isEmpty()) {
            printer.setSupportedColorModes(request.getSupportedColorModes());
        }
        if (request.getSupportedPaperSizes() != null && !request.getSupportedPaperSizes().isEmpty()) {
            printer.setSupportedPaperSizes(request.getSupportedPaperSizes());
        }
        if (request.getDuplexSupported() != null) {
            printer.setDuplexSupported(request.getDuplexSupported());
        }
        if (request.getAgentId() != null) {
            printer.setAgentId(request.getAgentId());
        }
        if (request.getEnabled() != null) {
            printer.setEnabled(request.getEnabled());
        }

        printer.setUpdatedAt(Instant.now());
        Printer updated = printerRepository.save(printer);
        log.info("[PRINTER] Updated printer {}", updated.getId());
        return PrinterResponse.from(updated);
    }

    public PrinterResponse getPrinterById(String id) {
        Printer printer = printerRepository.findById(id)
            .orElseThrow(() -> new PrinterNotFoundException("Printer not found with ID: " + id));
        return PrinterResponse.from(printer);
    }

    public List<PrinterResponse> getAllPrinters() {
        return printerRepository.findAll().stream()
            .map(PrinterResponse::from)
            .toList();
    }

    public List<PrinterResponse> getEnabledPrinters() {
        return printerRepository.findByEnabledTrue().stream()
            .map(PrinterResponse::from)
            .toList();
    }

    public PrinterResponse updatePrinterStatus(String id, PrinterStatus status) {
        Printer printer = printerRepository.findById(id)
            .orElseThrow(() -> new PrinterNotFoundException("Printer not found with ID: " + id));

        printer.setStatus(status);
        printer.setUpdatedAt(Instant.now());
        Printer updated = printerRepository.save(printer);
        log.info("[PRINTER] Printer {} status changed to {}", id, status);
        return PrinterResponse.from(updated);
    }

    public PrinterResponse setPrinterEnabled(String id, boolean enabled) {
        Printer printer = printerRepository.findById(id)
            .orElseThrow(() -> new PrinterNotFoundException("Printer not found with ID: " + id));

        printer.setEnabled(enabled);
        if (!enabled && printer.getStatus() != PrinterStatus.DISABLED) {
            printer.setStatus(PrinterStatus.DISABLED);
        } else if (enabled && printer.getStatus() == PrinterStatus.DISABLED) {
            printer.setStatus(PrinterStatus.OFFLINE);
        }
        printer.setUpdatedAt(Instant.now());
        Printer updated = printerRepository.save(printer);
        log.info("[PRINTER] Printer {} enabled set to {}", id, enabled);
        return PrinterResponse.from(updated);
    }

    public void recordHeartbeat(String id) {
        Printer printer = printerRepository.findById(id)
            .orElseThrow(() -> new PrinterNotFoundException("Printer not found with ID: " + id));

        printer.setLastHeartbeatAt(Instant.now());
        if (printer.getStatus() == PrinterStatus.OFFLINE) {
            printer.setStatus(PrinterStatus.ONLINE);
        }
        printerRepository.save(printer);
    }

    public void validateCompatibility(Printer printer, PrintJob job) {
        if (!Boolean.TRUE.equals(printer.getEnabled())) {
            throw new PrinterUnavailableException("Printer " + printer.getId() + " is disabled");
        }
        if (printer.getStatus() == PrinterStatus.DISABLED || printer.getStatus() == PrinterStatus.ERROR) {
            throw new PrinterUnavailableException("Printer " + printer.getId() + " is unavailable with status: " + printer.getStatus());
        }
        if (!printer.getSupportedColorModes().contains(job.getColorMode())) {
            throw new IncompatiblePrinterException("Printer " + printer.getId() + " does not support color mode: " + job.getColorMode());
        }
        if (!printer.getSupportedPaperSizes().contains(job.getPaperSize())) {
            throw new IncompatiblePrinterException("Printer " + printer.getId() + " does not support paper size: " + job.getPaperSize());
        }
        if (Boolean.TRUE.equals(job.getDuplex()) && !Boolean.TRUE.equals(printer.getDuplexSupported())) {
            throw new IncompatiblePrinterException("Printer " + printer.getId() + " does not support duplex printing");
        }
    }
}
