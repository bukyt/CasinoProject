package com.casino.bonus;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BonusEventConsumerTest {

    @Test
    void shouldAwardBonusCreditsEvery50Wagered() {
        BonusEventConsumer consumer = new BonusEventConsumer();

        BetPlaced event = new BetPlaced();
        event.setPlayerProfileId(1);
        event.setAmount(BigDecimal.valueOf(50));

        consumer.consume(event);

        assertThat(consumer.getPlayerCredits(1)).isEqualTo(10.0);
    }

    @Test
    void shouldAccumulateWageringAcrossMultipleEvents() {
        BonusEventConsumer consumer = new BonusEventConsumer();

        for (int i = 0; i < 5; i++) {
            BetPlaced event = new BetPlaced();
            event.setPlayerProfileId(1);
            event.setAmount(BigDecimal.valueOf(10));

            consumer.consume(event);
        }

        assertThat(consumer.getPlayerCredits(1)).isEqualTo(10.0);
    }

    @Test
    void shouldNotAwardCreditsBeforeThreshold() {
        BonusEventConsumer consumer = new BonusEventConsumer();

        BetPlaced event = new BetPlaced();
        event.setPlayerProfileId(1);
        event.setAmount(BigDecimal.valueOf(30));

        consumer.consume(event);

        assertThat(consumer.getPlayerCredits(1)).isEqualTo(0.0);
    }
}