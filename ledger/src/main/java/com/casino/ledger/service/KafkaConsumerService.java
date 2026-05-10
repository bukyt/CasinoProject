package com.casino.ledger.service;

import com.casino.CasinoConstants;
import com.casino.event.AbstractPlayerFinancialEvent;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class KafkaConsumerService {

    private final PlayerEventDispatcher playerEventDispatcher;
    private final Validator validator;

    public KafkaConsumerService(PlayerEventDispatcher playerEventDispatcher, Validator validator) {
        this.playerEventDispatcher = playerEventDispatcher;
        this.validator = validator;
    }

    @KafkaListener(topics = CasinoConstants.FINANCIAL_EVENTS_TOPIC_NAME)
    public void consume(AbstractPlayerFinancialEvent event) {
        Set<ConstraintViolation<AbstractPlayerFinancialEvent>> violations = validator.validate(event);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        playerEventDispatcher.dispatch(event);
    }
}
