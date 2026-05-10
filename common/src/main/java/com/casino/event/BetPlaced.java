package com.casino.event;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BetPlaced extends AbstractPlayerFinancialEvent {

    @Override
    public String toString() {
        return "BetPlaced{" +
                "amount=" + amount +
                ", playerProfileId=" + playerProfileId +
                '}';
    }
}
