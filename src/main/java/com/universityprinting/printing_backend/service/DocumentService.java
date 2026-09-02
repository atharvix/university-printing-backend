package com.universityprinting.printing_backend.service;

import com.universityprinting.printing_backend.dto.DocumentDownloadResponse;
import com.universityprinting.printing_backend.dto.DocumentResponse;
import com.universityprinting.printing_backend.exception.DocumentNotFoundException;
import com.universityprinting.printing_backend.exception.StorageException;
import com.universityprinting.printing_backend.exception.UnauthorizedDocumentAccessException;
import com.universityprinting.printing_backend.model.Document;
import com.universityprinting.printing_backend.repository.DocumentRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    private static final String RESOURCE_TYPE_RAW = "raw";
    private static final String CONTENT_TYPE_PDF = "application/pdf";

    private final DocumentRepository documentRepository;
    private final StorageService storageService;
    private final FileValidator fileValidator;

    public DocumentService(
        DocumentRepository documentRepository,
        StorageService storageService,
        FileValidator fileValidator
    ) {
        this.documentRepository = documentRepository;
        this.storageService = storageService;
        this.fileValidator = fileValidator;
    }

    public DocumentResponse uploadDocument(MultipartFile file, String ownerId) {
        fileValidator.validatePdf(file);
        String sanitizedFilename = fileValidator.sanitizeFilename(file.getOriginalFilename());
        String generatedStorageId = UUID.randomUUID().toString();
        String storageKey = "documents/" + ownerId + "/" + generatedStorageId;

        // 1. Upload to Cloud Storage
        storageService.uploadFile(file, storageKey, RESOURCE_TYPE_RAW);

        // 2. Persist Metadata in MongoDB (with rollback on DB failure)
        try {
            Instant now = Instant.now();
            Document document = new Document();
            document.setOwnerId(ownerId);
            document.setOriginalFileName(sanitizedFilename);
            document.setStorageKey(storageKey);
            document.setResourceType(RESOURCE_TYPE_RAW);
            document.setContentType(CONTENT_TYPE_PDF);
            document.setFileSize(file.getSize());
            document.setCreatedAt(now);
            document.setUpdatedAt(now);

            Document savedDocument = documentRepository.save(document);
            return DocumentResponse.from(savedDocument);
        } catch (Exception e) {
            log.error("Failed to save document metadata to MongoDB for ownerId: {}. Cleaning up storage object: {}", ownerId, storageKey);
            try {
                storageService.deleteFile(storageKey, RESOURCE_TYPE_RAW);
            } catch (Exception cleanupEx) {
                log.error("Failed to clean up orphaned Cloudinary asset after MongoDB save failure: {}", storageKey, cleanupEx);
            }
            throw new StorageException("Failed to save document metadata", e);
        }
    }

    public List<DocumentResponse> getDocumentsByOwner(String ownerId) {
        return documentRepository.findByOwnerId(ownerId)
            .stream()
            .map(DocumentResponse::from)
            .toList();
    }

    public DocumentResponse getDocumentByIdAndOwner(String documentId, String ownerId) {
        Document document = findDocumentAndVerifyOwnership(documentId, ownerId);
        return DocumentResponse.from(document);
    }

    public void deleteDocument(String documentId, String ownerId) {
        Document document = findDocumentAndVerifyOwnership(documentId, ownerId);
        storageService.deleteFile(document.getStorageKey(), document.getResourceType());
        documentRepository.deleteById(documentId);
    }

    public DocumentDownloadResponse getDocumentDownload(String documentId, String ownerId) {
        Document document = findDocumentAndVerifyOwnership(documentId, ownerId);
        String downloadUrl = storageService.generateDownloadUrl(document.getStorageKey(), document.getResourceType());
        return new DocumentDownloadResponse(document.getId(), document.getOriginalFileName(), downloadUrl);
    }

    private Document findDocumentAndVerifyOwnership(String documentId, String ownerId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + documentId));

        if (!document.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedDocumentAccessException("You are not authorized to access this document");
        }

        return document;
    }
}
