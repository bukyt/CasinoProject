package com.casino.ledger.service.handler;

import com.casino.event.AbstractPlayerEvent;

public interface PlayerEventHandler<E extends AbstractPlayerEvent> {

    Class<E> eventType();

    void handle(E event);
}
