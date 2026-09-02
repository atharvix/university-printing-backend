package com.universityprinting.printing_backend.security;

import com.universityprinting.printing_backend.model.AgentStatus;
import com.universityprinting.printing_backend.model.PrintAgent;
import com.universityprinting.printing_backend.repository.PrintAgentRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AgentAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AgentAuthenticationFilter.class);
    private static final String AGENT_KEY_HEADER = "X-Agent-Key";

    private final PrintAgentRepository printAgentRepository;

    public AgentAuthenticationFilter(PrintAgentRepository printAgentRepository) {
        this.printAgentRepository = printAgentRepository;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String rawAgentKey = request.getHeader(AGENT_KEY_HEADER);

        if (rawAgentKey != null && !rawAgentKey.isBlank()) {
            String keyHash = hashKey(rawAgentKey);
            Optional<PrintAgent> agentOpt = printAgentRepository.findByApiKeyHash(keyHash);

            if (agentOpt.isPresent()) {
                PrintAgent agent = agentOpt.get();
                if (agent.getStatus() != AgentStatus.DISABLED) {
                    AgentAuthenticationToken authentication = new AgentAuthenticationToken(agent);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("[AGENT AUTH] Successfully authenticated agent {}", agent.getId());
                } else {
                    log.warn("[AGENT AUTH] Agent {} is DISABLED", agent.getId());
                }
            } else {
                log.warn("[AGENT AUTH] Invalid agent key provided");
            }
        }

        filterChain.doFilter(request, response);
    }

    public static String hashKey(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash agent key", e);
        }
    }
}
