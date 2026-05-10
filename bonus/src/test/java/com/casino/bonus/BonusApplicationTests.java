package com.casino.bonus;


import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BonusEventConsumerTest {

    @Test
    void shouldAwardBonusCreditsEvery50Wagered() {

        BonusEventConsumer consumer = new BonusEventConsumer();

        String playerId = "player-1";

        BetPlaced event = new BetPlaced();
        event.setPlayerProfileId(playerId);
        event.setAmount(BigDecimal.valueOf(50));

        consumer.consume(event);

        assertThat(consumer.getPlayerCredits(playerId))
                .isEqualTo(10.0);
    }

    @Test
    void shouldAccumulateWageringAcrossMultipleEvents() {

        BonusEventConsumer consumer = new BonusEventConsumer();
        String playerId = "player-1";

        for (int i = 0; i < 5; i++) {
            BetPlaced event = new BetPlaced();
            event.setPlayerProfileId(playerId);
            event.setAmount(BigDecimal.valueOf(10));

            consumer.consume(event);
        }

        assertThat(consumer.getPlayerCredits(playerId))
                .isEqualTo(10.0);
    }

    @Test
    void shouldNotAwardCreditsBeforeThreshold() {

        BonusEventConsumer consumer = new BonusEventConsumer();
        String playerId = "player-1";

        BetPlaced event = new BetPlaced();
        event.setPlayerProfileId(playerId);
        event.setAmount(BigDecimal.valueOf(30));

        consumer.consume(event);

        assertThat(consumer.getPlayerCredits(playerId))
                .isEqualTo(0.0);
    }

    @Test
    void shouldSupportDebugCreditsManualAddition() {

        BonusEventConsumer consumer = new BonusEventConsumer();
        String playerId = "player-1";

        consumer.addDebugCredits(playerId, 25.0);

        assertThat(consumer.getPlayerCredits(playerId))
                .isEqualTo(25.0);
    }
}