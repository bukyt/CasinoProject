package com.casino.game;

import com.casino.event.BetPlaced;
import com.casino.event.BetSettled;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class GameEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public GameEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendBetPlaced(BetPlaced event) {
        kafkaTemplate.send(
                "betplaced",
                MessageBuilder.withPayload(event)
                        .setHeader("__TypeId__", "betplaced")
                        .build()
        );
    }

    public void sendBetSettled(BetSettled event) {
        kafkaTemplate.send(
                "betsettled",
                MessageBuilder.withPayload(event)
                        .setHeader("__TypeId__", "betsettled")
                        .build()
        );
    }
}