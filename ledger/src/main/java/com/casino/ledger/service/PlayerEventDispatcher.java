package com.casino.ledger.service;

import com.casino.event.AbstractPlayerEvent;
import com.casino.ledger.service.handler.PlayerEventHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PlayerEventDispatcher {

    private final Map<Class<? extends AbstractPlayerEvent>, PlayerEventHandler<? extends AbstractPlayerEvent>> handlers;

    public PlayerEventDispatcher(List<PlayerEventHandler<? extends AbstractPlayerEvent>> handlers) {
        this.handlers = handlers.stream()
                .collect(Collectors.toUnmodifiableMap(PlayerEventHandler::eventType, Function.identity()));
    }

    public void dispatch(AbstractPlayerEvent event) {
        PlayerEventHandler<? extends AbstractPlayerEvent> handler = handlers.get(event.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("No handler configured for event type " + event.getClass().getName());
        }
        handleEvent(handler, event);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void handleEvent(PlayerEventHandler handler, AbstractPlayerEvent event) {
        handler.handle(event);
    }
}
