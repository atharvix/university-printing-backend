package com.universityprinting.printing_backend.repository;

import com.universityprinting.printing_backend.model.Document;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends MongoRepository<Document, String> {

    List<Document> findByOwnerId(String ownerId);

    Optional<Document> findByIdAndOwnerId(String id, String ownerId);
}
