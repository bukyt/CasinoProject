package com.casino.authservice.events;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.casino.authservice.auth.model.AccountStatus;
import com.casino.event.AccountStatusChanged;

/**
 * Thin Kafka producer used by
 * {@link com.casino.authservice.auth.service.AuthService}.
 * Failures are logged but never thrown to callers — the HTTP response of the
 * admin
 * mutation does not depend on broker availability.
 */
@Component
public class AccountEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AccountEventPublisher.class);

    private final Optional<KafkaTemplate<String, Object>> kafkaTemplate;
    private final String topic;

    public AccountEventPublisher(
            Optional<KafkaTemplate<String, Object>> kafkaTemplate,
            @Value("${events.account-status.topic:auth.account-status}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishStatusChanged(String accountId, AccountStatus status) {
        AccountStatusChanged event = new AccountStatusChanged(accountId, status.name(), Instant.now());
        if (kafkaTemplate.isEmpty()) {
            log.debug("Kafka not configured; skipping {} for accountId={}", event, accountId);
            return;
        }
        try {
            kafkaTemplate.get().send(topic, accountId, event);
            log.info("Published AccountStatusChanged {} to topic {}", event, topic);
        } catch (Exception e) {
            log.warn("Failed to publish AccountStatusChanged for accountId={} (status={}): {}",
                    accountId, status, e.getMessage());
        }
    }
}
