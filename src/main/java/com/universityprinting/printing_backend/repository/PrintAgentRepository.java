package com.universityprinting.printing_backend.repository;

import com.universityprinting.printing_backend.model.AgentStatus;
import com.universityprinting.printing_backend.model.PrintAgent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrintAgentRepository extends MongoRepository<PrintAgent, String> {

    Optional<PrintAgent> findByApiKeyHash(String apiKeyHash);

    List<PrintAgent> findByStatus(AgentStatus status);
}
