package com.universityprinting.printing_backend.repository;

import com.universityprinting.printing_backend.model.Printer;
import com.universityprinting.printing_backend.model.PrinterStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrinterRepository extends MongoRepository<Printer, String> {

    List<Printer> findByAgentId(String agentId);

    List<Printer> findByStatus(PrinterStatus status);

    List<Printer> findByEnabledTrue();

    Optional<Printer> findByIdAndEnabledTrue(String id);
}
