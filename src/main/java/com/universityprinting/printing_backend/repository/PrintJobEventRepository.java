package com.universityprinting.printing_backend.repository;

import com.universityprinting.printing_backend.model.PrintJobEvent;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrintJobEventRepository extends MongoRepository<PrintJobEvent, String> {

    List<PrintJobEvent> findByPrintJobIdOrderByCreatedAtAsc(String printJobId);
}
