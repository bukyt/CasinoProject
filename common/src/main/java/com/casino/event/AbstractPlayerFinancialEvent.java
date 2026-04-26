package com.casino.event;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbstractPlayerFinancialEvent extends AbstractPlayerEvent {

    @NotNull(message = "Amount should be specified")
    @Positive(message = "Amount must be positive")
    public BigDecimal amount;

}
