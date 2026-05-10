package com.casino.bonus;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class BonusEventConsumerTest {

    @Test
    void shouldAwardBonusCreditsEvery50Wagered() {
        BonusEventConsumer consumer = new BonusEventConsumer();

        // Updated to use a UUID String
        String playerId = "85ffa295-8d61-47fd-844f-7b8add99aa38";
        
        BetPlaced event = new BetPlaced();
        event.setPlayerProfileId(playerId);
        event.setAmount(BigDecimal.valueOf(50));

        consumer.consume(event);

        assertThat(consumer.getPlayerCredits(playerId)).isEqualTo(10.0);
    }

    @Test
    void shouldAccumulateWageringAcrossMultipleEvents() {
        BonusEventConsumer consumer = new BonusEventConsumer();
        String playerId = "85ffa295-8d61-47fd-844f-7b8add99aa38";

        for (int i = 0; i < 5; i++) {
            BetPlaced event = new BetPlaced();
            event.setPlayerProfileId(playerId);
            event.setAmount(BigDecimal.valueOf(10));

            consumer.consume(event);
        }

        assertThat(consumer.getPlayerCredits(playerId)).isEqualTo(10.0);
    }

    @Test
    void shouldNotAwardCreditsBeforeThreshold() {
        BonusEventConsumer consumer = new BonusEventConsumer();
        String playerId = "85ffa295-8d61-47fd-844f-7b8add99aa38";

        BetPlaced event = new BetPlaced();
        event.setPlayerProfileId(playerId);
        event.setAmount(BigDecimal.valueOf(30));

        consumer.consume(event);

        assertThat(consumer.getPlayerCredits(playerId)).isEqualTo(0.0);
    }

    @Test
    void shouldSupportDebugCreditsManualAddition() {
        BonusEventConsumer consumer = new BonusEventConsumer();
        String playerId = "85ffa295-8d61-47fd-844f-7b8add99aa38";

        consumer.addDebugCredits(playerId, 25.0);

        assertThat(consumer.getPlayerCredits(playerId)).isEqualTo(25.0);
    }
}