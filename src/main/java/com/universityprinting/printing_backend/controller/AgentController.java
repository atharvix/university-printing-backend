package com.universityprinting.printing_backend.controller;

import com.universityprinting.printing_backend.dto.AgentHeartbeatRequest;
import com.universityprinting.printing_backend.dto.AgentJobActionRequest;
import com.universityprinting.printing_backend.dto.AgentJobResponse;
import com.universityprinting.printing_backend.dto.AgentRegistrationResponse;
import com.universityprinting.printing_backend.dto.AgentResponse;
import com.universityprinting.printing_backend.dto.PrintJobResponse;
import com.universityprinting.printing_backend.dto.RegisterAgentRequest;
import com.universityprinting.printing_backend.model.PrintAgent;
import com.universityprinting.printing_backend.security.AgentAuthenticationToken;
import com.universityprinting.printing_backend.service.AgentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgentRegistrationResponse> registerAgent(
        @Valid @RequestBody RegisterAgentRequest request
    ) {
        AgentRegistrationResponse response = agentService.registerAgent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/heartbeat")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<Void> heartbeat(
        @RequestBody(required = false) AgentHeartbeatRequest request,
        Authentication authentication
    ) {
        String agentId = extractAgentId(authentication);
        agentService.heartbeat(agentId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/jobs/poll")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<AgentJobResponse> pollJob(Authentication authentication) {
        String agentId = extractAgentId(authentication);
        AgentJobResponse response = agentService.pollNextJob(agentId);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/jobs/{id}/ack")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<PrintJobResponse> acknowledgeJob(
        @PathVariable("id") String jobId,
        Authentication authentication
    ) {
        String agentId = extractAgentId(authentication);
        PrintJobResponse response = agentService.acknowledgeJob(agentId, jobId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/jobs/{id}/complete")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<PrintJobResponse> completeJob(
        @PathVariable("id") String jobId,
        Authentication authentication
    ) {
        String agentId = extractAgentId(authentication);
        PrintJobResponse response = agentService.completeJob(agentId, jobId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/jobs/{id}/fail")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<PrintJobResponse> failJob(
        @PathVariable("id") String jobId,
        @RequestBody(required = false) AgentJobActionRequest request,
        Authentication authentication
    ) {
        String agentId = extractAgentId(authentication);
        String reason = request != null ? request.getReason() : "Failed by print agent";
        PrintJobResponse response = agentService.failJob(agentId, jobId, reason);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AgentResponse>> getAllAgents() {
        return ResponseEntity.ok(agentService.getAllAgents());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgentResponse> getAgentById(@PathVariable("id") String id) {
        return ResponseEntity.ok(agentService.getAgentById(id));
    }

    private String extractAgentId(Authentication authentication) {
        if (authentication instanceof AgentAuthenticationToken token) {
            PrintAgent agent = token.getAgent();
            if (agent != null && agent.getId() != null) {
                return agent.getId();
            }
        }
        if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) {
            return authentication.getName();
        }
        throw new AccessDeniedException("Print agent identity is missing");
    }
}
