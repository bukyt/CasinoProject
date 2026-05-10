package com.casino.event;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event for when the player deposits funds to the platform.
 */
@Data
@NoArgsConstructor
public class DepositCompleted extends AbstractPlayerFinancialEvent {

    @Override
    public String toString() {
        return "DepositCompleted{" +
                "amount=" + amount +
                ", playerProfileId=" + playerProfileId +
                '}';
    }
}
