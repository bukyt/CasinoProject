package com.casino.bonus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {"betplaced"})
class BonusIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private BonusEventConsumer consumer;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void endToEndBonusFlow_shouldAccumulateAndExposeBonus() throws Exception {

        String playerId = "player-1";

        BetPlaced event = new BetPlaced();
        event.setPlayerProfileId(playerId);
        event.setAmount(BigDecimal.valueOf(50));

        // ✅ FIX: send JSON string instead of object
        kafkaTemplate.send("betplaced", objectMapper.writeValueAsString(event));

        waitForBonus(playerId, 10.0);

        Boolean active = rest.getForObject(
                "/bonuses/players/" + playerId + "/has-active-bonus",
                Boolean.class
        );

        assertThat(active).isFalse();

        Double credits = consumer.getPlayerCredits(playerId);
        assertThat(credits).isEqualTo(10.0);
    }

    @Test
    void shouldAccumulateMultipleBetsIntoSingleBonusBlock() throws Exception {

        String playerId = "player-2";

        for (int i = 0; i < 5; i++) {

            BetPlaced event = new BetPlaced();
            event.setPlayerProfileId(playerId);
            event.setAmount(BigDecimal.valueOf(10));

            // ✅ FIX: JSON string
            kafkaTemplate.send("betplaced", objectMapper.writeValueAsString(event));
        }

        waitForBonus(playerId, 10.0);

        assertThat(consumer.getPlayerCredits(playerId))
                .isEqualTo(10.0);
    }

    // ----------------------------
    // deterministic polling
    // ----------------------------
    private void waitForBonus(String playerId, double expected) throws InterruptedException {

        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 5000) {

            Double value = consumer.getPlayerCredits(playerId);

            if (value != null && value >= expected) {
                return;
            }

            Thread.sleep(50);
        }

        throw new AssertionError("Bonus not reached in time");
    }
}