package com.casino.bonus;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class BonusEventConsumerTest {

    @Test
    void shouldAwardBonusCreditsEvery50Wagered() {

        BonusEventConsumer consumer = new BonusEventConsumer(new com.fasterxml.jackson.databind.ObjectMapper());

        Map<String, Object> event = new HashMap<>();
        event.put("playerProfileId", "player-1");
        event.put("amount", BigDecimal.valueOf(50));

        consumer.process(event);

        assertThat(consumer.getPlayerCredits(111029429))
                .isEqualTo(10.0);
    }

    @Test
    void shouldAccumulateWageringAcrossMultipleEvents() {

        BonusEventConsumer consumer = new BonusEventConsumer(new com.fasterxml.jackson.databind.ObjectMapper());

        for (int i = 0; i < 5; i++) {
            Map<String, Object> event = new HashMap<>();
            event.put("playerProfileId", "player-1");
            event.put("amount", BigDecimal.valueOf(10));

            consumer.process(event);
        }

        assertThat(consumer.getPlayerCredits(111029429))
                .isEqualTo(10.0);
    }

    @Test
    void shouldNotAwardCreditsBeforeThreshold() {

        BonusEventConsumer consumer = new BonusEventConsumer(new com.fasterxml.jackson.databind.ObjectMapper());

        Map<String, Object> event = new HashMap<>();
        event.put("playerProfileId", 111029429);
        event.put("amount", BigDecimal.valueOf(30));

        consumer.process(event);

        assertThat(consumer.getPlayerCredits(111029429))
                .isEqualTo(0.0);
    }

    @Test
    void shouldSupportDebugCreditsManualAddition() {

        BonusEventConsumer consumer = new BonusEventConsumer(new com.fasterxml.jackson.databind.ObjectMapper());

        consumer.addDebugCredits(111029429, 25.0);

        assertThat(consumer.getPlayerCredits(111029429))
                .isEqualTo(25.0);
    }
}