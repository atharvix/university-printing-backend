package com.universityprinting.printing_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.universityprinting.printing_backend.dto.DocumentDownloadResponse;
import com.universityprinting.printing_backend.dto.DocumentResponse;
import com.universityprinting.printing_backend.exception.DocumentNotFoundException;
import com.universityprinting.printing_backend.exception.StorageException;
import com.universityprinting.printing_backend.exception.UnauthorizedDocumentAccessException;
import com.universityprinting.printing_backend.model.Document;
import com.universityprinting.printing_backend.repository.DocumentRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private StorageService storageService;

    private FileValidator fileValidator;
    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        fileValidator = new FileValidator();
        documentService = new DocumentService(documentRepository, storageService, fileValidator);
    }

    @Test
    void uploadDocument_Success_StoresMetadataAndUploadsToCloud() {
        byte[] validPdf = "%PDF-1.4 test document".getBytes(StandardCharsets.US_ASCII);
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "thesis.pdf",
            "application/pdf",
            validPdf
        );
        String ownerId = "student-123";

        doNothing().when(storageService).uploadFile(eq(file), anyString(), eq("raw"));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            doc.setId("doc-id-001");
            return doc;
        });

        DocumentResponse response = documentService.uploadDocument(file, ownerId);

        assertNotNull(response);
        assertEquals("doc-id-001", response.id());
        assertEquals("student-123", response.ownerId());
        assertEquals("thesis.pdf", response.originalFileName());
        assertEquals("application/pdf", response.contentType());
        assertEquals(validPdf.length, response.fileSize());
        assertNotNull(response.createdAt());

        ArgumentCaptor<String> storageKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(storageService).uploadFile(eq(file), storageKeyCaptor.capture(), eq("raw"));
        String capturedStorageKey = storageKeyCaptor.getValue();
        assertTrue(capturedStorageKey.startsWith("documents/student-123/"));

        ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(docCaptor.capture());
        Document savedDoc = docCaptor.getValue();
        assertEquals("student-123", savedDoc.getOwnerId());
        assertEquals(capturedStorageKey, savedDoc.getStorageKey());
    }

    @Test
    void uploadDocument_MongoSaveFails_CleansUpUploadedStorageAsset() {
        byte[] validPdf = "%PDF-1.4 test document".getBytes(StandardCharsets.US_ASCII);
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "thesis.pdf",
            "application/pdf",
            validPdf
        );
        String ownerId = "student-123";

        doNothing().when(storageService).uploadFile(eq(file), anyString(), eq("raw"));
        when(documentRepository.save(any(Document.class))).thenThrow(new RuntimeException("Database timeout"));

        assertThrows(StorageException.class, () -> documentService.uploadDocument(file, ownerId));

        // Verify storage cleanup / delete is invoked
        verify(storageService).deleteFile(anyString(), eq("raw"));
    }

    @Test
    void getDocumentsByOwner_ReturnsUserDocuments() {
        String ownerId = "student-123";
        Document doc1 = new Document("doc-1", ownerId, "file1.pdf", "key1", "raw", "application/pdf", 1000L, Instant.now(), Instant.now());
        Document doc2 = new Document("doc-2", ownerId, "file2.pdf", "key2", "raw", "application/pdf", 2000L, Instant.now(), Instant.now());

        when(documentRepository.findByOwnerId(ownerId)).thenReturn(List.of(doc1, doc2));

        List<DocumentResponse> result = documentService.getDocumentsByOwner(ownerId);

        assertEquals(2, result.size());
        assertEquals("doc-1", result.get(0).id());
        assertEquals("doc-2", result.get(1).id());
    }

    @Test
    void getDocumentByIdAndOwner_Success() {
        String ownerId = "student-123";
        Document doc = new Document("doc-1", ownerId, "file1.pdf", "key1", "raw", "application/pdf", 1000L, Instant.now(), Instant.now());

        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));

        DocumentResponse response = documentService.getDocumentByIdAndOwner("doc-1", ownerId);

        assertNotNull(response);
        assertEquals("doc-1", response.id());
        assertEquals(ownerId, response.ownerId());
    }

    @Test
    void getDocumentByIdAndOwner_DifferentOwner_ThrowsUnauthorizedException() {
        String ownerId = "student-123";
        String differentOwnerId = "student-999";
        Document doc = new Document("doc-1", ownerId, "file1.pdf", "key1", "raw", "application/pdf", 1000L, Instant.now(), Instant.now());

        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));

        assertThrows(
            UnauthorizedDocumentAccessException.class,
            () -> documentService.getDocumentByIdAndOwner("doc-1", differentOwnerId)
        );
    }

    @Test
    void deleteDocument_Success_DeletesStorageAndDbRecord() {
        String ownerId = "student-123";
        Document doc = new Document("doc-1", ownerId, "file1.pdf", "key1", "raw", "application/pdf", 1000L, Instant.now(), Instant.now());

        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));

        documentService.deleteDocument("doc-1", ownerId);

        verify(storageService).deleteFile("key1", "raw");
        verify(documentRepository).deleteById("doc-1");
    }

    @Test
    void deleteDocument_DifferentOwner_ThrowsUnauthorizedExceptionAndDoesNotDelete() {
        String ownerId = "student-123";
        String differentOwnerId = "student-999";
        Document doc = new Document("doc-1", ownerId, "file1.pdf", "key1", "raw", "application/pdf", 1000L, Instant.now(), Instant.now());

        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));

        assertThrows(
            UnauthorizedDocumentAccessException.class,
            () -> documentService.deleteDocument("doc-1", differentOwnerId)
        );

        verify(storageService, never()).deleteFile(anyString(), anyString());
        verify(documentRepository, never()).deleteById(anyString());
    }

    @Test
    void getDocumentDownload_Success_GeneratesDownloadUrl() {
        String ownerId = "student-123";
        Document doc = new Document("doc-1", ownerId, "file1.pdf", "key1", "raw", "application/pdf", 1000L, Instant.now(), Instant.now());

        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));
        when(storageService.generateDownloadUrl("key1", "raw")).thenReturn("https://res.cloudinary.com/demo/raw/upload/key1");

        DocumentDownloadResponse response = documentService.getDocumentDownload("doc-1", ownerId);

        assertNotNull(response);
        assertEquals("doc-1", response.id());
        assertEquals("file1.pdf", response.originalFileName());
        assertEquals("https://res.cloudinary.com/demo/raw/upload/key1", response.downloadUrl());
    }
}
