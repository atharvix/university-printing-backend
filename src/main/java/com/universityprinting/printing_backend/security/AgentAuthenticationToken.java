package com.universityprinting.printing_backend.security;

import com.universityprinting.printing_backend.model.PrintAgent;
import java.util.Collection;
import java.util.List;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class AgentAuthenticationToken extends AbstractAuthenticationToken {

    private final PrintAgent agent;

    public AgentAuthenticationToken(PrintAgent agent) {
        super(List.of(new SimpleGrantedAuthority("ROLE_AGENT")));
        this.agent = agent;
        setAuthenticated(true);
    }

    public AgentAuthenticationToken(PrintAgent agent, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.agent = agent;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return agent;
    }

    @Override
    public String getName() {
        return agent != null ? agent.getId() : "";
    }

    public PrintAgent getAgent() {
        return agent;
    }
}
