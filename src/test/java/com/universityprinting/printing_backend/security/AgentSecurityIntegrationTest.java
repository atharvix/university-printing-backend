package com.universityprinting.printing_backend.security;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cloudinary.Cloudinary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.universityprinting.printing_backend.model.AgentStatus;
import com.universityprinting.printing_backend.model.PrintAgent;
import com.universityprinting.printing_backend.repository.DocumentRepository;
import com.universityprinting.printing_backend.repository.PaymentRepository;
import com.universityprinting.printing_backend.repository.PrintAgentRepository;
import com.universityprinting.printing_backend.repository.PrintJobEventRepository;
import com.universityprinting.printing_backend.repository.PrintJobRepository;
import com.universityprinting.printing_backend.repository.PrinterRepository;
import com.universityprinting.printing_backend.repository.UserRepository;
import com.universityprinting.printing_backend.service.AgentService;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = "spring.mongodb.uri=mongodb://localhost:27017/test_db")
class AgentSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private DocumentRepository documentRepository;

    @MockitoBean
    private PrintJobRepository printJobRepository;

    @MockitoBean
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PrinterRepository printerRepository;

    @MockitoBean
    private PrintJobEventRepository printJobEventRepository;

    @MockitoBean
    private PrintAgentRepository printAgentRepository;

    @MockitoBean
    private AgentService agentService;

    @MockitoBean
    private Cloudinary cloudinary;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void heartbeat_WithoutAgentKey_Returns401Unauthorized() throws Exception {
        mockMvc.perform(post("/api/agent/heartbeat"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void heartbeat_WithValidAgentKey_Returns200Ok() throws Exception {
        String rawKey = "agk_test_valid_key_12345";
        String keyHash = AgentAuthenticationFilter.hashKey(rawKey);

        PrintAgent agent = new PrintAgent("agent-1", "Agent 1", "host-1", keyHash, AgentStatus.ACTIVE, Set.of(), null, Instant.now(), Instant.now());
        when(printAgentRepository.findByApiKeyHash(keyHash)).thenReturn(Optional.of(agent));

        mockMvc.perform(post("/api/agent/heartbeat")
                .header("X-Agent-Key", rawKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk());
    }

    @Test
    void pollJob_WithValidAgentKey_Returns204NoContentWhenEmpty() throws Exception {
        String rawKey = "agk_test_valid_key_12345";
        String keyHash = AgentAuthenticationFilter.hashKey(rawKey);

        PrintAgent agent = new PrintAgent("agent-1", "Agent 1", "host-1", keyHash, AgentStatus.ACTIVE, Set.of(), null, Instant.now(), Instant.now());
        when(printAgentRepository.findByApiKeyHash(keyHash)).thenReturn(Optional.of(agent));
        when(agentService.pollNextJob("agent-1")).thenReturn(null);

        mockMvc.perform(get("/api/agent/jobs/poll")
                .header("X-Agent-Key", rawKey))
            .andExpect(status().isNoContent());
    }
}
