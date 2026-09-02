package com.universityprinting.printing_backend.repository;

import com.universityprinting.printing_backend.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    List<Payment> findByOwnerId(String ownerId);

    List<Payment> findByPrintJobId(String printJobId);

    Optional<Payment> findByIdAndOwnerId(String id, String ownerId);

    Optional<Payment> findByProviderOrderId(String providerOrderId);
}
