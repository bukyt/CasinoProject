package com.casino.bonus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {"betplaced"})
class BonusIntegrationTest {

    @Autowired
    private KafkaTemplate<String, BetPlaced> kafkaTemplate;

    @Autowired
    private WebTestClient client;

    @Test
    void endToEndBonusFlow() throws Exception {
        // Use the same UUID format your frontend is sending
        String playerId = "85ffa295-8d61-47fd-844f-7b8add99aa38";

        // 1. Send bet event via Kafka
        BetPlaced event = new BetPlaced();
        event.setPlayerProfileId(playerId); // Now accepts String
        event.setAmount(BigDecimal.valueOf(50));

        kafkaTemplate.send("betplaced", event);

        // Allow time for the @KafkaListener in BonusEventConsumer to process
        Thread.sleep(1500); 

        // 2. Check credits via REST API
        Double credits = client.get()
                .uri("/bonuses/players/" + playerId + "/credits")
                .exchange()
                .expectStatus().isOk() // This would fail with 400 if we sent an Integer
                .expectBody(Double.class)
                .returnResult()
                .getResponseBody();

        assertThat(credits).isEqualTo(10.0);
    }
}