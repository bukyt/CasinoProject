package com.casino.event;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Event for when the player withdraws funds from the platform
 */
@Data
@NoArgsConstructor
public class WithdrawalProcessed extends AbstractPlayerFinancialEvent {

    @Override
    public String toString() {
        return "WithdrawalProcessed{" +
                "amount=" + amount +
                ", playerProfileId=" + playerProfileId +
                '}';
    }
}
