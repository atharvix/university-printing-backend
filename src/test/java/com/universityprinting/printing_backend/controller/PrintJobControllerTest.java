package com.universityprinting.printing_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.universityprinting.printing_backend.dto.CreatePrintJobRequest;
import com.universityprinting.printing_backend.dto.PrintJobResponse;
import com.universityprinting.printing_backend.exception.GlobalExceptionHandler;
import com.universityprinting.printing_backend.exception.InvalidPrintJobStateException;
import com.universityprinting.printing_backend.exception.PrintJobNotFoundException;
import com.universityprinting.printing_backend.exception.UnauthorizedPrintJobAccessException;
import com.universityprinting.printing_backend.model.ColorMode;
import com.universityprinting.printing_backend.model.PaperSize;
import com.universityprinting.printing_backend.model.PrintJobStatus;
import com.universityprinting.printing_backend.service.PrintJobService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PrintJobControllerTest {

    @Mock
    private PrintJobService printJobService;

    private MockMvc mockMvc;
    private Jwt mockJwt;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        PrintJobController printJobController = new PrintJobController(printJobService);
        mockMvc = MockMvcBuilders.standaloneSetup(printJobController)
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

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void createPrintJob_Success_Returns201Created() throws Exception {
        CreatePrintJobRequest request = new CreatePrintJobRequest("doc-1", 2, ColorMode.COLOR, PaperSize.A4, true, 5);
        PrintJobResponse response = new PrintJobResponse(
            "job-1", "student-123", "doc-1", 2, ColorMode.COLOR, PaperSize.A4, true, 5,
            new BigDecimal("45.00"), PrintJobStatus.QUEUED, Instant.now(), Instant.now()
        );

        when(printJobService.createPrintJob(any(CreatePrintJobRequest.class), eq("student-123"))).thenReturn(response);

        mockMvc.perform(post("/api/print-jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("job-1"))
            .andExpect(jsonPath("$.ownerId").value("student-123"))
            .andExpect(jsonPath("$.documentId").value("doc-1"))
            .andExpect(jsonPath("$.price").value(45.00))
            .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void createPrintJob_InvalidInput_Returns400BadRequest() throws Exception {
        CreatePrintJobRequest invalidRequest = new CreatePrintJobRequest("", 0, null, null, null, 0);

        mockMvc.perform(post("/api/print-jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation failed"))
            .andExpect(jsonPath("$.details.documentId").exists())
            .andExpect(jsonPath("$.details.copies").exists())
            .andExpect(jsonPath("$.details.colorMode").exists())
            .andExpect(jsonPath("$.details.paperSize").exists())
            .andExpect(jsonPath("$.details.duplex").exists())
            .andExpect(jsonPath("$.details.pageCount").exists());
    }

    @Test
    void getMyPrintJobs_Returns200Ok() throws Exception {
        PrintJobResponse job = new PrintJobResponse(
            "job-1", "student-123", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 3,
            new BigDecimal("6.00"), PrintJobStatus.QUEUED, Instant.now(), Instant.now()
        );
        when(printJobService.getPrintJobsByOwner("student-123")).thenReturn(List.of(job));

        mockMvc.perform(get("/api/print-jobs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("job-1"))
            .andExpect(jsonPath("$[0].ownerId").value("student-123"));
    }

    @Test
    void getPrintJobById_Success_Returns200Ok() throws Exception {
        PrintJobResponse job = new PrintJobResponse(
            "job-1", "student-123", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 3,
            new BigDecimal("6.00"), PrintJobStatus.QUEUED, Instant.now(), Instant.now()
        );
        when(printJobService.getPrintJobByIdAndOwner("job-1", "student-123")).thenReturn(job);

        mockMvc.perform(get("/api/print-jobs/job-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("job-1"))
            .andExpect(jsonPath("$.ownerId").value("student-123"));
    }

    @Test
    void getPrintJobById_NotFound_Returns404() throws Exception {
        when(printJobService.getPrintJobByIdAndOwner("job-unknown", "student-123"))
            .thenThrow(new PrintJobNotFoundException("Print job not found with ID: job-unknown"));

        mockMvc.perform(get("/api/print-jobs/job-unknown"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not found"))
            .andExpect(jsonPath("$.message").value("Print job not found with ID: job-unknown"));
    }

    @Test
    void cancelPrintJob_PostCancel_Returns200Ok() throws Exception {
        PrintJobResponse job = new PrintJobResponse(
            "job-1", "student-123", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 3,
            new BigDecimal("6.00"), PrintJobStatus.CANCELLED, Instant.now(), Instant.now()
        );
        when(printJobService.cancelPrintJob("job-1", "student-123")).thenReturn(job);

        mockMvc.perform(post("/api/print-jobs/job-1/cancel"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("job-1"))
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelPrintJob_Delete_Returns200Ok() throws Exception {
        PrintJobResponse job = new PrintJobResponse(
            "job-1", "student-123", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 3,
            new BigDecimal("6.00"), PrintJobStatus.CANCELLED, Instant.now(), Instant.now()
        );
        when(printJobService.cancelPrintJob("job-1", "student-123")).thenReturn(job);

        mockMvc.perform(delete("/api/print-jobs/job-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("job-1"))
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelPrintJob_InvalidState_Returns409Conflict() throws Exception {
        when(printJobService.cancelPrintJob("job-1", "student-123"))
            .thenThrow(new InvalidPrintJobStateException("Cannot cancel print job in status: PROCESSING"));

        mockMvc.perform(post("/api/print-jobs/job-1/cancel"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Conflict"))
            .andExpect(jsonPath("$.message").value("Cannot cancel print job in status: PROCESSING"));
    }

    @Test
    void cancelPrintJob_Unauthorized_Returns403Forbidden() throws Exception {
        when(printJobService.cancelPrintJob("job-1", "student-123"))
            .thenThrow(new UnauthorizedPrintJobAccessException("You do not have permission to cancel this print job"));

        mockMvc.perform(post("/api/print-jobs/job-1/cancel"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("Forbidden"))
            .andExpect(jsonPath("$.message").value("You do not have permission to cancel this print job"));
    }

    @Test
    void getPrintJobsByStatus_Returns200Ok() throws Exception {
        PrintJobResponse job = new PrintJobResponse(
            "job-1", "student-123", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 3,
            new BigDecimal("6.00"), PrintJobStatus.QUEUED, Instant.now(), Instant.now()
        );
        when(printJobService.getPrintJobsByStatus(PrintJobStatus.QUEUED)).thenReturn(List.of(job));

        mockMvc.perform(get("/api/print-jobs/status/QUEUED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("job-1"))
            .andExpect(jsonPath("$[0].status").value("QUEUED"));
    }
}
