package com.universityprinting.printing_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.universityprinting.printing_backend.dto.AgentHeartbeatRequest;
import com.universityprinting.printing_backend.dto.AgentJobActionRequest;
import com.universityprinting.printing_backend.dto.AgentJobResponse;
import com.universityprinting.printing_backend.dto.AgentRegistrationResponse;
import com.universityprinting.printing_backend.dto.PrintJobResponse;
import com.universityprinting.printing_backend.dto.RegisterAgentRequest;
import com.universityprinting.printing_backend.exception.GlobalExceptionHandler;
import com.universityprinting.printing_backend.model.AgentStatus;
import com.universityprinting.printing_backend.model.ColorMode;
import com.universityprinting.printing_backend.model.PaperSize;
import com.universityprinting.printing_backend.model.PrintAgent;
import com.universityprinting.printing_backend.model.PrintJobStatus;
import com.universityprinting.printing_backend.security.AgentAuthenticationToken;
import com.universityprinting.printing_backend.service.AgentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AgentControllerTest {

    @Mock
    private AgentService agentService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AgentAuthenticationToken mockAuth;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        AgentController agentController = new AgentController(agentService);

        PrintAgent mockAgent = new PrintAgent("agent-123", "Agent 1", "host-1", "hash", AgentStatus.ACTIVE, Set.of("ptr-1"), null, Instant.now(), Instant.now());
        mockAuth = new AgentAuthenticationToken(mockAgent);

        mockMvc = MockMvcBuilders.standaloneSetup(agentController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new org.springframework.web.method.support.HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                    return parameter.getParameterType().isAssignableFrom(Authentication.class);
                }

                @Override
                public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                              org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                                              org.springframework.web.context.request.NativeWebRequest webRequest,
                                              org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                    return mockAuth;
                }
            })
            .build();
    }

    @Test
    void registerAgent_Success_Returns201Created() throws Exception {
        RegisterAgentRequest request = new RegisterAgentRequest("Lab-Agent", "host-1", Set.of("ptr-1"));
        AgentRegistrationResponse response = new AgentRegistrationResponse("agent-123", "Lab-Agent", "agk_secret123", AgentStatus.ACTIVE);

        when(agentService.registerAgent(any(RegisterAgentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/agent/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("agent-123"))
            .andExpect(jsonPath("$.rawApiKey").value("agk_secret123"));
    }

    @Test
    void heartbeat_Success_Returns200Ok() throws Exception {
        AgentHeartbeatRequest request = new AgentHeartbeatRequest(AgentStatus.ACTIVE, null);
        doNothing().when(agentService).heartbeat(eq("agent-123"), any(AgentHeartbeatRequest.class));

        mockMvc.perform(post("/api/agent/heartbeat")
                .principal(mockAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void pollJob_Found_Returns200Ok() throws Exception {
        AgentJobResponse job = new AgentJobResponse("job-1", "doc-1", "http://storage/1.pdf", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5, "ptr-1");
        when(agentService.pollNextJob("agent-123")).thenReturn(job);

        mockMvc.perform(get("/api/agent/jobs/poll")
                .principal(mockAuth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("job-1"))
            .andExpect(jsonPath("$.documentDownloadUrl").value("http://storage/1.pdf"));
    }

    @Test
    void pollJob_NotFound_Returns204NoContent() throws Exception {
        when(agentService.pollNextJob("agent-123")).thenReturn(null);

        mockMvc.perform(get("/api/agent/jobs/poll")
                .principal(mockAuth))
            .andExpect(status().isNoContent());
    }

    @Test
    void acknowledgeJob_Returns200Ok() throws Exception {
        PrintJobResponse response = new PrintJobResponse("job-1", "student-1", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5, new BigDecimal("10.00"), PrintJobStatus.PRINTING, Instant.now(), Instant.now());
        when(agentService.acknowledgeJob("agent-123", "job-1")).thenReturn(response);

        mockMvc.perform(post("/api/agent/jobs/job-1/ack")
                .principal(mockAuth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PRINTING"));
    }

    @Test
    void completeJob_Returns200Ok() throws Exception {
        PrintJobResponse response = new PrintJobResponse("job-1", "student-1", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5, new BigDecimal("10.00"), PrintJobStatus.COMPLETED, Instant.now(), Instant.now());
        when(agentService.completeJob("agent-123", "job-1")).thenReturn(response);

        mockMvc.perform(post("/api/agent/jobs/job-1/complete")
                .principal(mockAuth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void failJob_Returns200Ok() throws Exception {
        AgentJobActionRequest request = new AgentJobActionRequest("Paper jam");
        PrintJobResponse response = new PrintJobResponse("job-1", "student-1", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5, new BigDecimal("10.00"), PrintJobStatus.FAILED, Instant.now(), Instant.now());
        when(agentService.failJob("agent-123", "job-1", "Paper jam")).thenReturn(response);

        mockMvc.perform(post("/api/agent/jobs/job-1/fail")
                .principal(mockAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("FAILED"));
    }
}
