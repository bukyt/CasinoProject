package com.casino.bonus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.Map;

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

        // 1. send bet events
        BetPlaced event = new BetPlaced();
        event.setPlayerProfileId(1);
        event.setAmount(BigDecimal.valueOf(50));

        kafkaTemplate.send("betplaced", event);

        Thread.sleep(1000); // allow async consumer processing

        // 2. check credits via REST
        Double credits = client.get()
                .uri("/bonuses/players/1/credits")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Double.class)
                .returnResult()
                .getResponseBody();

        assertThat(credits).isEqualTo(10.0);
    }
}