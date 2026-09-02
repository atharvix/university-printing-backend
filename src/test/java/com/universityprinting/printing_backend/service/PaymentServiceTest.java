package com.universityprinting.printing_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.universityprinting.printing_backend.dto.CreatePaymentRequest;
import com.universityprinting.printing_backend.dto.PaymentResponse;
import com.universityprinting.printing_backend.dto.PaymentWebhookRequest;
import com.universityprinting.printing_backend.dto.VerifyPaymentRequest;
import com.universityprinting.printing_backend.exception.DuplicatePaymentException;
import com.universityprinting.printing_backend.exception.InvalidPrintJobStateException;
import com.universityprinting.printing_backend.exception.PaymentNotFoundException;
import com.universityprinting.printing_backend.exception.PaymentVerificationException;
import com.universityprinting.printing_backend.exception.PrintJobNotFoundException;
import com.universityprinting.printing_backend.exception.UnauthorizedPaymentAccessException;
import com.universityprinting.printing_backend.exception.UnauthorizedPrintJobAccessException;
import com.universityprinting.printing_backend.model.ColorMode;
import com.universityprinting.printing_backend.model.PaperSize;
import com.universityprinting.printing_backend.model.Payment;
import com.universityprinting.printing_backend.model.PaymentStatus;
import com.universityprinting.printing_backend.model.PrintJob;
import com.universityprinting.printing_backend.model.PrintJobStatus;
import com.universityprinting.printing_backend.repository.PaymentRepository;
import com.universityprinting.printing_backend.repository.PrintJobRepository;
import com.universityprinting.printing_backend.service.payment.PaymentGateway;
import com.universityprinting.printing_backend.service.payment.PaymentOrderResult;
import com.universityprinting.printing_backend.service.payment.PaymentVerificationResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PrintJobRepository printJobRepository;

    @Mock
    private PaymentGateway paymentGateway;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, printJobRepository, paymentGateway);
    }

    @Test
    void createPayment_Success() {
        CreatePaymentRequest request = new CreatePaymentRequest("job-123");
        PrintJob job = createSampleJob("job-123", "student-1", new BigDecimal("25.00"), PrintJobStatus.QUEUED, false);

        when(printJobRepository.findById("job-123")).thenReturn(Optional.of(job));
        when(paymentGateway.createOrder("job-123", new BigDecimal("25.00"), "INR"))
            .thenReturn(new PaymentOrderResult("order_123", new BigDecimal("25.00"), "INR", "MOCK_GATEWAY"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId("pay-999");
            return p;
        });

        PaymentResponse response = paymentService.createPayment(request, "student-1");

        assertNotNull(response);
        assertEquals("pay-999", response.getId());
        assertEquals("job-123", response.getPrintJobId());
        assertEquals("student-1", response.getOwnerId());
        assertEquals(new BigDecimal("25.00"), response.getAmount());
        assertEquals(PaymentStatus.CREATED, response.getStatus());
        assertEquals("order_123", response.getProviderOrderId());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createPayment_PrintJobNotFound_ThrowsPrintJobNotFoundException() {
        CreatePaymentRequest request = new CreatePaymentRequest("job-unknown");
        when(printJobRepository.findById("job-unknown")).thenReturn(Optional.empty());

        assertThrows(
            PrintJobNotFoundException.class,
            () -> paymentService.createPayment(request, "student-1")
        );
    }

    @Test
    void createPayment_UnauthorizedOwner_ThrowsUnauthorizedPrintJobAccessException() {
        CreatePaymentRequest request = new CreatePaymentRequest("job-123");
        PrintJob job = createSampleJob("job-123", "student-2", new BigDecimal("25.00"), PrintJobStatus.QUEUED, false);
        when(printJobRepository.findById("job-123")).thenReturn(Optional.of(job));

        assertThrows(
            UnauthorizedPrintJobAccessException.class,
            () -> paymentService.createPayment(request, "student-1")
        );
    }

    @Test
    void createPayment_AlreadyPaidJob_ThrowsDuplicatePaymentException() {
        CreatePaymentRequest request = new CreatePaymentRequest("job-123");
        PrintJob job = createSampleJob("job-123", "student-1", new BigDecimal("25.00"), PrintJobStatus.QUEUED, true);
        when(printJobRepository.findById("job-123")).thenReturn(Optional.of(job));

        assertThrows(
            DuplicatePaymentException.class,
            () -> paymentService.createPayment(request, "student-1")
        );
    }

    @Test
    void createPayment_NonQueuedJob_ThrowsInvalidPrintJobStateException() {
        CreatePaymentRequest request = new CreatePaymentRequest("job-123");
        PrintJob job = createSampleJob("job-123", "student-1", new BigDecimal("25.00"), PrintJobStatus.PROCESSING, false);
        when(printJobRepository.findById("job-123")).thenReturn(Optional.of(job));

        assertThrows(
            InvalidPrintJobStateException.class,
            () -> paymentService.createPayment(request, "student-1")
        );
    }

    @Test
    void verifyPayment_Success_SetsPaymentPaidAndJobQueueEligible() {
        VerifyPaymentRequest request = new VerifyPaymentRequest("order_123", "pay_txn_456", "sig_valid");
        Payment payment = new Payment("pay-1", "job-123", "student-1", new BigDecimal("25.00"), PaymentStatus.CREATED, "MOCK", "order_123", null, null, Instant.now(), Instant.now());
        PrintJob job = createSampleJob("job-123", "student-1", new BigDecimal("25.00"), PrintJobStatus.QUEUED, false);

        when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(payment));
        when(paymentGateway.verifyPayment("order_123", "pay_txn_456", "sig_valid"))
            .thenReturn(new PaymentVerificationResult(true, "pay_txn_456", "Success"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(printJobRepository.findById("job-123")).thenReturn(Optional.of(job));
        when(printJobRepository.save(any(PrintJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.verifyPayment("pay-1", request, "student-1");

        assertNotNull(response);
        assertEquals(PaymentStatus.PAID, response.getStatus());
        assertEquals("pay_txn_456", response.getProviderPaymentId());
        assertTrue(job.getQueueEligible());
        verify(printJobRepository).save(job);
    }

    @Test
    void verifyPayment_Idempotent_AlreadyPaid() {
        VerifyPaymentRequest request = new VerifyPaymentRequest("order_123", "pay_txn_456", "sig_valid");
        Payment payment = new Payment("pay-1", "job-123", "student-1", new BigDecimal("25.00"), PaymentStatus.PAID, "MOCK", "order_123", "pay_txn_456", "sig_valid", Instant.now(), Instant.now());

        when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.verifyPayment("pay-1", request, "student-1");

        assertEquals(PaymentStatus.PAID, response.getStatus());
        verify(paymentGateway, never()).verifyPayment(anyString(), anyString(), anyString());
    }

    @Test
    void verifyPayment_VerificationFailed_SetsFailedAndThrows() {
        VerifyPaymentRequest request = new VerifyPaymentRequest("order_123", "pay_txn_456", "sig_invalid");
        Payment payment = new Payment("pay-1", "job-123", "student-1", new BigDecimal("25.00"), PaymentStatus.CREATED, "MOCK", "order_123", null, null, Instant.now(), Instant.now());

        when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(payment));
        when(paymentGateway.verifyPayment("order_123", "pay_txn_456", "sig_invalid"))
            .thenReturn(new PaymentVerificationResult(false, "pay_txn_456", "Invalid signature"));

        assertThrows(
            PaymentVerificationException.class,
            () -> paymentService.verifyPayment("pay-1", request, "student-1")
        );
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    void handleWebhook_Success_MarksPaidAndQueueEligible() {
        PaymentWebhookRequest request = new PaymentWebhookRequest("payment.captured", "order_123", "pay_txn_789", "PAID");
        Payment payment = new Payment("pay-1", "job-123", "student-1", new BigDecimal("25.00"), PaymentStatus.CREATED, "MOCK", "order_123", null, null, Instant.now(), Instant.now());
        PrintJob job = createSampleJob("job-123", "student-1", new BigDecimal("25.00"), PrintJobStatus.QUEUED, false);

        when(paymentGateway.verifyWebhookSignature("raw_payload", "sig_header")).thenReturn(true);
        when(paymentRepository.findByProviderOrderId("order_123")).thenReturn(Optional.of(payment));
        when(printJobRepository.findById("job-123")).thenReturn(Optional.of(job));

        paymentService.handleWebhook("raw_payload", "sig_header", request);

        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertEquals("pay_txn_789", payment.getProviderPaymentId());
        assertTrue(job.getQueueEligible());
        verify(paymentRepository).save(payment);
        verify(printJobRepository).save(job);
    }

    @Test
    void handleWebhook_InvalidSignature_ThrowsPaymentVerificationException() {
        PaymentWebhookRequest request = new PaymentWebhookRequest("payment.captured", "order_123", "pay_txn_789", "PAID");
        when(paymentGateway.verifyWebhookSignature("raw_payload", "invalid_sig")).thenReturn(false);

        assertThrows(
            PaymentVerificationException.class,
            () -> paymentService.handleWebhook("raw_payload", "invalid_sig", request)
        );
    }

    @Test
    void getPaymentByIdAndOwner_Success() {
        Payment payment = new Payment("pay-1", "job-123", "student-1", new BigDecimal("25.00"), PaymentStatus.PAID, "MOCK", "order_123", "pay_txn_456", "sig", Instant.now(), Instant.now());
        when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentByIdAndOwner("pay-1", "student-1");

        assertNotNull(response);
        assertEquals("pay-1", response.getId());
    }

    @Test
    void getPaymentsByOwner_Success() {
        Payment payment = new Payment("pay-1", "job-123", "student-1", new BigDecimal("25.00"), PaymentStatus.PAID, "MOCK", "order_123", "pay_txn_456", "sig", Instant.now(), Instant.now());
        when(paymentRepository.findByOwnerId("student-1")).thenReturn(List.of(payment));

        List<PaymentResponse> results = paymentService.getPaymentsByOwner("student-1");

        assertEquals(1, results.size());
        assertEquals("pay-1", results.get(0).getId());
    }

    private PrintJob createSampleJob(String id, String ownerId, BigDecimal price, PrintJobStatus status, boolean queueEligible) {
        return new PrintJob(
            id,
            ownerId,
            "doc-1",
            1,
            ColorMode.BLACK_WHITE,
            PaperSize.A4,
            false,
            5,
            price,
            status,
            queueEligible,
            null,
            null,
            Instant.now(),
            Instant.now()
        );
    }
}
