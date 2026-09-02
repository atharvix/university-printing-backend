package com.universityprinting.printing_backend.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cloudinary.Cloudinary;
import com.universityprinting.printing_backend.dto.DocumentResponse;
import com.universityprinting.printing_backend.model.Role;
import com.universityprinting.printing_backend.model.User;
import com.universityprinting.printing_backend.repository.DocumentRepository;
import com.universityprinting.printing_backend.repository.UserRepository;
import com.universityprinting.printing_backend.service.DocumentService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = "spring.mongodb.uri=mongodb://localhost:27017/test_db")
class DocumentSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private DocumentRepository documentRepository;

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private Cloudinary cloudinary;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void uploadDocument_Unauthenticated_Returns401Unauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            "%PDF-1.4 test".getBytes(StandardCharsets.US_ASCII)
        );

        mockMvc.perform(multipart("/api/documents").file(file))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void getDocuments_Unauthenticated_Returns401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/documents"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void deleteDocument_Unauthenticated_Returns401Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/documents/doc-1"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void uploadDocument_AuthenticatedWithJwt_Returns201Created() throws Exception {
        User user = new User("student-123", "Student User", "student@university.edu", "+1234567890", Role.STUDENT, "hash", Instant.now(), Instant.now());
        String token = jwtService.generateToken(user);

        byte[] pdfBytes = "%PDF-1.4 test".getBytes(StandardCharsets.US_ASCII);
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", pdfBytes);

        DocumentResponse response = new DocumentResponse("doc-1", "student-123", "test.pdf", "application/pdf", pdfBytes.length, Instant.now(), Instant.now());
        when(documentService.uploadDocument(any(), eq("student-123"))).thenReturn(response);

        mockMvc.perform(multipart("/api/documents")
                .file(file)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("doc-1"))
            .andExpect(jsonPath("$.ownerId").value("student-123"));
    }

    @Test
    void getDocuments_AuthenticatedWithJwt_Returns200Ok() throws Exception {
        User user = new User("student-123", "Student User", "student@university.edu", "+1234567890", Role.STUDENT, "hash", Instant.now(), Instant.now());
        String token = jwtService.generateToken(user);

        when(documentService.getDocumentsByOwner("student-123")).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/documents")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}
