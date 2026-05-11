package com.casino.dto;


import com.casino.model.PaymentStatus;
import com.casino.model.PaymentType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentDto(
    Long paymentId,
    Long playerProfileId,
    PaymentType type,
    BigDecimal amount,
    String provider,
    PaymentStatus status,
    OffsetDateTime createdDate
) {
}