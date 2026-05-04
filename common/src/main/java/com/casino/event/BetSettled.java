package com.casino.event;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Event for when the game round ends.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BetSettled extends AbstractPlayerFinancialEvent {

    @NotNull(message = "gameEndingType must be specified")
    public GameEndingType gameEndingType;

    @Override
    public String toString() {
        return "BetSettled{" +
                "gameEndingType=" + gameEndingType +
                ", amount=" + amount +
                ", playerProfileId=" + playerProfileId +
                '}';
    }
}
