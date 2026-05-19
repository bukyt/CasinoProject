package com.casino.game;

import com.casino.CasinoConstants;
import com.casino.event.BetPlaced;
import com.casino.event.BetSettled;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class GameEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public GameEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendBetPlaced(BetPlaced event) {
        kafkaTemplate.send(
            CasinoConstants.FINANCIAL_EVENTS_TOPIC_NAME,
            String.valueOf(event.getPlayerProfileId()),
            event
        );
    }

    public void sendBetSettled(BetSettled event) {
        kafkaTemplate.send(
            CasinoConstants.FINANCIAL_EVENTS_TOPIC_NAME,
            String.valueOf(event.getPlayerProfileId()),
            event
        );
    }
}
