package com.universityprinting.printing_backend.controller;

import com.universityprinting.printing_backend.dto.CreatePrintJobRequest;
import com.universityprinting.printing_backend.dto.PrintJobResponse;
import com.universityprinting.printing_backend.model.PrintJobStatus;
import com.universityprinting.printing_backend.service.PrintJobService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/print-jobs")
public class PrintJobController {

    private final PrintJobService printJobService;

    public PrintJobController(PrintJobService printJobService) {
        this.printJobService = printJobService;
    }

    @PostMapping
    public ResponseEntity<PrintJobResponse> createPrintJob(
        @Valid @RequestBody CreatePrintJobRequest request,
        @AuthenticationPrincipal Jwt jwt,
        Authentication authentication
    ) {
        String ownerId = extractUserId(jwt, authentication);
        PrintJobResponse response = printJobService.createPrintJob(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PrintJobResponse>> getMyPrintJobs(
        @AuthenticationPrincipal Jwt jwt,
        Authentication authentication
    ) {
        String ownerId = extractUserId(jwt, authentication);
        List<PrintJobResponse> response = printJobService.getPrintJobsByOwner(ownerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrintJobResponse> getPrintJobById(
        @PathVariable("id") String id,
        @AuthenticationPrincipal Jwt jwt,
        Authentication authentication
    ) {
        String ownerId = extractUserId(jwt, authentication);
        PrintJobResponse response = printJobService.getPrintJobByIdAndOwner(id, ownerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PrintJobResponse> cancelPrintJob(
        @PathVariable("id") String id,
        @AuthenticationPrincipal Jwt jwt,
        Authentication authentication
    ) {
        String ownerId = extractUserId(jwt, authentication);
        PrintJobResponse response = printJobService.cancelPrintJob(id, ownerId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PrintJobResponse> cancelPrintJobViaDelete(
        @PathVariable("id") String id,
        @AuthenticationPrincipal Jwt jwt,
        Authentication authentication
    ) {
        String ownerId = extractUserId(jwt, authentication);
        PrintJobResponse response = printJobService.cancelPrintJob(id, ownerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<List<PrintJobResponse>> getPrintJobsByStatus(
        @PathVariable("status") PrintJobStatus status
    ) {
        List<PrintJobResponse> response = printJobService.getPrintJobsByStatus(status);
        return ResponseEntity.ok(response);
    }

    private String extractUserId(Jwt jwt, Authentication authentication) {
        if (jwt != null && jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
            return jwt.getSubject();
        }
        if (authentication != null && authentication.getName() != null) {
            return authentication.getName();
        }
        throw new AccessDeniedException("Authentication principal is missing");
    }
}
