package com.universityprinting.printing_backend.controller;

import com.universityprinting.printing_backend.dto.DocumentDownloadResponse;
import com.universityprinting.printing_backend.dto.DocumentResponse;
import com.universityprinting.printing_backend.service.DocumentService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(
        @RequestParam("file") MultipartFile file,
        @AuthenticationPrincipal Jwt jwt,
        Authentication authentication
    ) {
        String ownerId = extractUserId(jwt, authentication);
        DocumentResponse response = documentService.uploadDocument(file, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getMyDocuments(
        @AuthenticationPrincipal Jwt jwt,
        Authentication authentication
    ) {
        String ownerId = extractUserId(jwt, authentication);
        List<DocumentResponse> response = documentService.getDocumentsByOwner(ownerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentById(
        @PathVariable("id") String id,
        @AuthenticationPrincipal Jwt jwt,
        Authentication authentication
    ) {
        String ownerId = extractUserId(jwt, authentication);
        DocumentResponse response = documentService.getDocumentByIdAndOwner(id, ownerId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
        @PathVariable("id") String id,
        @AuthenticationPrincipal Jwt jwt,
        Authentication authentication
    ) {
        String ownerId = extractUserId(jwt, authentication);
        documentService.deleteDocument(id, ownerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<DocumentDownloadResponse> getDocumentDownload(
        @PathVariable("id") String id,
        @AuthenticationPrincipal Jwt jwt,
        Authentication authentication
    ) {
        String ownerId = extractUserId(jwt, authentication);
        DocumentDownloadResponse response = documentService.getDocumentDownload(id, ownerId);
        return ResponseEntity.ok(response);
    }

    private String extractUserId(Jwt jwt, Authentication authentication) {
        if (jwt != null && jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
            return jwt.getSubject();
        }
        if (authentication != null && authentication.getName() != null) {
            return authentication.getName();
        }
        throw new org.springframework.security.access.AccessDeniedException("Authentication principal is missing");
    }
}
