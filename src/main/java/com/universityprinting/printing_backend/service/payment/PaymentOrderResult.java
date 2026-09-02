package com.universityprinting.printing_backend.service.payment;

import java.math.BigDecimal;

public record PaymentOrderResult(
    String providerOrderId,
    BigDecimal amount,
    String currency,
    String provider
) {}
