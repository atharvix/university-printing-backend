package com.universityprinting.printing_backend.service;

import com.universityprinting.printing_backend.dto.CreatePaymentRequest;
import com.universityprinting.printing_backend.dto.PaymentResponse;
import com.universityprinting.printing_backend.dto.PaymentWebhookRequest;
import com.universityprinting.printing_backend.dto.VerifyPaymentRequest;
import com.universityprinting.printing_backend.exception.DuplicatePaymentException;
import com.universityprinting.printing_backend.exception.InvalidPaymentStateException;
import com.universityprinting.printing_backend.exception.InvalidPrintJobStateException;
import com.universityprinting.printing_backend.exception.PaymentNotFoundException;
import com.universityprinting.printing_backend.exception.PaymentVerificationException;
import com.universityprinting.printing_backend.exception.PrintJobNotFoundException;
import com.universityprinting.printing_backend.exception.UnauthorizedPaymentAccessException;
import com.universityprinting.printing_backend.exception.UnauthorizedPrintJobAccessException;
import com.universityprinting.printing_backend.model.Payment;
import com.universityprinting.printing_backend.model.PaymentStatus;
import com.universityprinting.printing_backend.model.PrintJob;
import com.universityprinting.printing_backend.model.PrintJobStatus;
import com.universityprinting.printing_backend.repository.PaymentRepository;
import com.universityprinting.printing_backend.repository.PrintJobRepository;
import com.universityprinting.printing_backend.service.payment.PaymentGateway;
import com.universityprinting.printing_backend.service.payment.PaymentOrderResult;
import com.universityprinting.printing_backend.service.payment.PaymentVerificationResult;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PrintJobRepository printJobRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(
        PaymentRepository paymentRepository,
        PrintJobRepository printJobRepository,
        PaymentGateway paymentGateway
    ) {
        this.paymentRepository = paymentRepository;
        this.printJobRepository = printJobRepository;
        this.paymentGateway = paymentGateway;
    }

    public PaymentResponse createPayment(CreatePaymentRequest request, String ownerId) {
        PrintJob printJob = printJobRepository.findById(request.getPrintJobId())
            .orElseThrow(() -> new PrintJobNotFoundException("Print job not found with ID: " + request.getPrintJobId()));

        if (!ownerId.equals(printJob.getOwnerId())) {
            throw new UnauthorizedPrintJobAccessException("You do not have permission to pay for this print job");
        }

        if (printJob.getStatus() != PrintJobStatus.QUEUED) {
            throw new InvalidPrintJobStateException("Cannot create payment for job in status: " + printJob.getStatus());
        }

        if (Boolean.TRUE.equals(printJob.getQueueEligible())) {
            throw new DuplicatePaymentException("Print job is already paid and eligible for printing");
        }

        PaymentOrderResult orderResult = paymentGateway.createOrder(
            printJob.getId(),
            printJob.getPrice(),
            "INR"
        );

        Instant now = Instant.now();
        Payment payment = new Payment(
            null,
            printJob.getId(),
            ownerId,
            printJob.getPrice(),
            PaymentStatus.CREATED,
            orderResult.provider(),
            orderResult.providerOrderId(),
            null,
            null,
            now,
            now
        );

        Payment savedPayment = paymentRepository.save(payment);
        log.info("[PAYMENT] Created payment record {} for print job {}", savedPayment.getId(), printJob.getId());
        return PaymentResponse.from(savedPayment);
    }

    public PaymentResponse verifyPayment(String paymentId, VerifyPaymentRequest request, String ownerId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

        if (!ownerId.equals(payment.getOwnerId())) {
            throw new UnauthorizedPaymentAccessException("You do not have permission to verify this payment");
        }

        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("[PAYMENT] Payment {} already marked PAID. Returning response idempotently.", paymentId);
            return PaymentResponse.from(payment);
        }

        PaymentVerificationResult result = paymentGateway.verifyPayment(
            request.getProviderOrderId(),
            request.getProviderPaymentId(),
            request.getProviderSignature()
        );

        if (!result.verified()) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(Instant.now());
            paymentRepository.save(payment);
            throw new PaymentVerificationException("Payment verification failed: " + result.message());
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setProviderOrderId(request.getProviderOrderId());
        payment.setProviderPaymentId(request.getProviderPaymentId());
        payment.setProviderSignature(request.getProviderSignature());
        payment.setUpdatedAt(Instant.now());
        Payment savedPayment = paymentRepository.save(payment);

        PrintJob printJob = printJobRepository.findById(payment.getPrintJobId())
            .orElseThrow(() -> new PrintJobNotFoundException("Associated print job not found: " + payment.getPrintJobId()));

        printJob.setQueueEligible(true);
        printJob.setUpdatedAt(Instant.now());
        printJobRepository.save(printJob);

        log.info("[PAYMENT] Payment {} verified as PAID. Print job {} is now queue-eligible.", payment.getId(), printJob.getId());
        return PaymentResponse.from(savedPayment);
    }

    public void handleWebhook(String rawPayload, String signatureHeader, PaymentWebhookRequest request) {
        if (!paymentGateway.verifyWebhookSignature(rawPayload, signatureHeader)) {
            log.warn("[PAYMENT WEBHOOK] Invalid webhook cryptographic signature received");
            throw new PaymentVerificationException("Invalid webhook signature");
        }

        if (request == null || request.getProviderOrderId() == null) {
            log.warn("[PAYMENT WEBHOOK] Missing provider order ID in webhook payload");
            return;
        }

        Payment payment = paymentRepository.findByProviderOrderId(request.getProviderOrderId()).orElse(null);
        if (payment == null) {
            log.warn("[PAYMENT WEBHOOK] No payment found for provider order ID: {}", request.getProviderOrderId());
            return;
        }

        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("[PAYMENT WEBHOOK] Payment {} is already PAID. Idempotently ignoring duplicate webhook.", payment.getId());
            return;
        }

        if ("PAID".equalsIgnoreCase(request.getStatus()) || "SUCCESS".equalsIgnoreCase(request.getStatus())) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setProviderPaymentId(request.getProviderPaymentId());
            payment.setUpdatedAt(Instant.now());
            paymentRepository.save(payment);

            PrintJob job = printJobRepository.findById(payment.getPrintJobId()).orElse(null);
            if (job != null) {
                job.setQueueEligible(true);
                job.setUpdatedAt(Instant.now());
                printJobRepository.save(job);
            }
            log.info("[PAYMENT WEBHOOK] Processed PAID event for payment {}", payment.getId());
        } else if ("FAILED".equalsIgnoreCase(request.getStatus())) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(Instant.now());
            paymentRepository.save(payment);
            log.info("[PAYMENT WEBHOOK] Processed FAILED event for payment {}", payment.getId());
        }
    }

    public PaymentResponse getPaymentByIdAndOwner(String id, String ownerId) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + id));

        if (!ownerId.equals(payment.getOwnerId())) {
            throw new UnauthorizedPaymentAccessException("You do not have access to this payment");
        }

        return PaymentResponse.from(payment);
    }

    public List<PaymentResponse> getPaymentsByOwner(String ownerId) {
        return paymentRepository.findByOwnerId(ownerId)
            .stream()
            .map(PaymentResponse::from)
            .toList();
    }
}
