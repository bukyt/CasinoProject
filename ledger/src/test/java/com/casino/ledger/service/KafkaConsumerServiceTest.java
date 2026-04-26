package com.casino.ledger.service;

import com.casino.event.BetSettled;
import com.casino.event.GameEndingType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class KafkaConsumerServiceTest {

    private PlayerEventDispatcher playerEventDispatcher;
    private Validator validator;
    private KafkaConsumerService kafkaConsumerService;

    @BeforeEach
    void setUp() {
        playerEventDispatcher = mock(PlayerEventDispatcher.class);
        validator = mock(Validator.class);
        kafkaConsumerService = new KafkaConsumerService(playerEventDispatcher, validator);
    }

    @Test
    void consumeDispatchesValidEvent() {
        BetSettled event = settledEvent(GameEndingType.WIN);
        when(validator.validate(event)).thenReturn(Set.of());

        kafkaConsumerService.consume(event);

        verify(playerEventDispatcher).dispatch(event);
    }

    @Test
    void consumeThrowsWhenValidationFails() {
        BetSettled event = settledEvent(null);
        @SuppressWarnings("unchecked")
        ConstraintViolation<BetSettled> violation = mock(ConstraintViolation.class);
        when(validator.validate(event)).thenReturn(Set.of(violation));

        assertThrows(ConstraintViolationException.class, () -> kafkaConsumerService.consume(event));

        verifyNoInteractions(playerEventDispatcher);
    }

    @Test
    void consumeThrowsWhenAmountIsNotPositive() {
        BetSettled event = settledEvent(GameEndingType.WIN);
        event.setAmount(BigDecimal.ZERO);
        @SuppressWarnings("unchecked")
        ConstraintViolation<BetSettled> violation = mock(ConstraintViolation.class);
        when(validator.validate(event)).thenReturn(Set.of(violation));

        assertThrows(ConstraintViolationException.class, () -> kafkaConsumerService.consume(event));

        verifyNoInteractions(playerEventDispatcher);
    }

    private BetSettled settledEvent(GameEndingType gameEndingType) {
        BetSettled event = new BetSettled();
        event.setPlayerProfileId(42);
        event.setAmount(new BigDecimal("15.00"));
        event.setGameEndingType(gameEndingType);
        return event;
    }
}
