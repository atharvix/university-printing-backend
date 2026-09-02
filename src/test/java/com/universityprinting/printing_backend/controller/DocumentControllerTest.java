package com.universityprinting.printing_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.universityprinting.printing_backend.dto.DocumentDownloadResponse;
import com.universityprinting.printing_backend.dto.DocumentResponse;
import com.universityprinting.printing_backend.exception.DocumentNotFoundException;
import com.universityprinting.printing_backend.exception.GlobalExceptionHandler;
import com.universityprinting.printing_backend.exception.InvalidFileException;
import com.universityprinting.printing_backend.exception.UnauthorizedDocumentAccessException;
import com.universityprinting.printing_backend.service.DocumentService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private DocumentService documentService;

    private MockMvc mockMvc;
    private Jwt mockJwt;

    @BeforeEach
    void setUp() {
        DocumentController documentController = new DocumentController(documentService);
        mockMvc = MockMvcBuilders.standaloneSetup(documentController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new org.springframework.web.method.support.HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                    return parameter.getParameterType().isAssignableFrom(Jwt.class);
                }

                @Override
                public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                              org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                                              org.springframework.web.context.request.NativeWebRequest webRequest,
                                              org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                    return mockJwt;
                }
            })
            .build();

        mockJwt = Jwt.withTokenValue("mock-token")
            .header("alg", "HS256")
            .subject("student-123")
            .claim("email", "student@university.edu")
            .claim("role", "STUDENT")
            .build();
    }

    @Test
    void uploadDocument_Success_Returns201Created() throws Exception {
        byte[] pdfBytes = "%PDF-1.4 content".getBytes(StandardCharsets.US_ASCII);
        MockMultipartFile file = new MockMultipartFile("file", "lab_report.pdf", "application/pdf", pdfBytes);
        DocumentResponse response = new DocumentResponse("doc-1", "student-123", "lab_report.pdf", "application/pdf", pdfBytes.length, Instant.now(), Instant.now());

        when(documentService.uploadDocument(any(), eq("student-123"))).thenReturn(response);

        mockMvc.perform(multipart("/api/documents").file(file))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("doc-1"))
            .andExpect(jsonPath("$.ownerId").value("student-123"))
            .andExpect(jsonPath("$.originalFileName").value("lab_report.pdf"))
            .andExpect(jsonPath("$.contentType").value("application/pdf"))
            .andExpect(jsonPath("$.storageKey").doesNotExist());
    }

    @Test
    void uploadDocument_InvalidPdf_Returns400BadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "fake.pdf", "application/pdf", "not a pdf".getBytes());

        when(documentService.uploadDocument(any(), eq("student-123")))
            .thenThrow(new InvalidFileException("File content does not match standard PDF magic bytes (%PDF-)"));

        mockMvc.perform(multipart("/api/documents").file(file))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Invalid file"))
            .andExpect(jsonPath("$.message").value("File content does not match standard PDF magic bytes (%PDF-)"));
    }

    @Test
    void getMyDocuments_Success_ReturnsDocumentList() throws Exception {
        DocumentResponse doc1 = new DocumentResponse("doc-1", "student-123", "a.pdf", "application/pdf", 1000L, Instant.now(), Instant.now());
        when(documentService.getDocumentsByOwner("student-123")).thenReturn(List.of(doc1));

        mockMvc.perform(get("/api/documents"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("doc-1"))
            .andExpect(jsonPath("$[0].originalFileName").value("a.pdf"));
    }

    @Test
    void getDocumentById_NotFound_Returns404() throws Exception {
        when(documentService.getDocumentByIdAndOwner("doc-999", "student-123"))
            .thenThrow(new DocumentNotFoundException("Document not found with ID: doc-999"));

        mockMvc.perform(get("/api/documents/doc-999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not found"))
            .andExpect(jsonPath("$.message").value("Document not found with ID: doc-999"));
    }

    @Test
    void getDocumentById_Unauthorized_Returns403() throws Exception {
        when(documentService.getDocumentByIdAndOwner("doc-1", "student-123"))
            .thenThrow(new UnauthorizedDocumentAccessException("You are not authorized to access this document"));

        mockMvc.perform(get("/api/documents/doc-1"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("Forbidden"))
            .andExpect(jsonPath("$.message").value("You are not authorized to access this document"));
    }

    @Test
    void deleteDocument_Success_Returns204NoContent() throws Exception {
        doNothing().when(documentService).deleteDocument("doc-1", "student-123");

        mockMvc.perform(delete("/api/documents/doc-1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void getDocumentDownload_Success_ReturnsDownloadUrl() throws Exception {
        DocumentDownloadResponse response = new DocumentDownloadResponse("doc-1", "lab.pdf", "https://res.cloudinary.com/demo/raw/upload/doc-1");
        when(documentService.getDocumentDownload("doc-1", "student-123")).thenReturn(response);

        mockMvc.perform(get("/api/documents/doc-1/download"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("doc-1"))
            .andExpect(jsonPath("$.originalFileName").value("lab.pdf"))
            .andExpect(jsonPath("$.downloadUrl").value("https://res.cloudinary.com/demo/raw/upload/doc-1"));
    }
}
