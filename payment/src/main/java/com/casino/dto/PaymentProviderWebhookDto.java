package com.casino.dto;


import com.casino.model.PaymentStatus;

public record PaymentProviderWebhookDto(
    Long paymentId,
    String provider,
    String providerTransactionId,
    PaymentStatus status,
    String message
) {
}