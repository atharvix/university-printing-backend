package com.universityprinting.printing_backend.repository;

import com.universityprinting.printing_backend.model.PrintJob;
import com.universityprinting.printing_backend.model.PrintJobStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrintJobRepository extends MongoRepository<PrintJob, String> {

    List<PrintJob> findByOwnerId(String ownerId);

    Optional<PrintJob> findByIdAndOwnerId(String id, String ownerId);

    List<PrintJob> findByDocumentId(String documentId);

    List<PrintJob> findByStatus(PrintJobStatus status);
}
