package com.casino.dto;

import java.math.BigDecimal;

public record CreatePaymentDto(
    Long playerProfileId,
    BigDecimal amount,
    String provider
) {
}
