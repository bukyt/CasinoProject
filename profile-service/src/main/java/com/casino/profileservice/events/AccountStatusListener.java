package com.casino.profileservice.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.casino.event.AccountStatusChanged;
import com.casino.profileservice.users.service.PlayerProfileService;

/**
 * Consumes {@link AccountStatusChanged} events from auth-service and forwards the
 * change into the player profile (ACTIVE → ACTIVE, SUSPENDED → INACTIVE).
 *
 * <p>This is the one async hop between auth-service and profile-service. The
 * admin's PATCH on auth-service returns immediately; the profile gets updated
 * eventually when Kafka delivers the message.</p>
 */
@Component
public class AccountStatusListener {

    private static final Logger log = LoggerFactory.getLogger(AccountStatusListener.class);

    private final PlayerProfileService playerProfileService;

    public AccountStatusListener(PlayerProfileService playerProfileService) {
        this.playerProfileService = playerProfileService;
    }

    @KafkaListener(topics = "${events.account-status.topic:auth.account-status}",
            groupId = "${spring.kafka.consumer.group-id:profile-service}")
    public void onAccountStatusChanged(AccountStatusChanged event) {
        if (event == null || event.getAccountId() == null || event.getStatus() == null) {
            log.warn("Ignoring malformed AccountStatusChanged: {}", event);
            return;
        }
        log.info("Received AccountStatusChanged {}", event);
        try {
            playerProfileService.applyAccountStatus(event.getAccountId(), event.getStatus());
        } catch (Exception e) {
            log.error("Failed to apply AccountStatusChanged {}: {}", event, e.getMessage());
        }
    }
}
