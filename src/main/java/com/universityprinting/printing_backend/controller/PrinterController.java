package com.universityprinting.printing_backend.controller;

import com.universityprinting.printing_backend.dto.CreatePrinterRequest;
import com.universityprinting.printing_backend.dto.PrinterResponse;
import com.universityprinting.printing_backend.dto.UpdatePrinterRequest;
import com.universityprinting.printing_backend.model.PrinterStatus;
import com.universityprinting.printing_backend.service.PrinterService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/printers")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
public class PrinterController {

    private final PrinterService printerService;

    public PrinterController(PrinterService printerService) {
        this.printerService = printerService;
    }

    @PostMapping
    public ResponseEntity<PrinterResponse> createPrinter(@Valid @RequestBody CreatePrinterRequest request) {
        PrinterResponse response = printerService.createPrinter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PrinterResponse>> getAllPrinters() {
        return ResponseEntity.ok(printerService.getAllPrinters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrinterResponse> getPrinterById(@PathVariable("id") String id) {
        return ResponseEntity.ok(printerService.getPrinterById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PrinterResponse> updatePrinter(
        @PathVariable("id") String id,
        @RequestBody UpdatePrinterRequest request
    ) {
        return ResponseEntity.ok(printerService.updatePrinter(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PrinterResponse> updatePrinterStatus(
        @PathVariable("id") String id,
        @RequestParam("status") PrinterStatus status
    ) {
        return ResponseEntity.ok(printerService.updatePrinterStatus(id, status));
    }

    @PostMapping("/{id}/enable")
    public ResponseEntity<PrinterResponse> enablePrinter(@PathVariable("id") String id) {
        return ResponseEntity.ok(printerService.setPrinterEnabled(id, true));
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<PrinterResponse> disablePrinter(@PathVariable("id") String id) {
        return ResponseEntity.ok(printerService.setPrinterEnabled(id, false));
    }
}
