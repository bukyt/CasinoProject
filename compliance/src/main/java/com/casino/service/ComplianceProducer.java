package com.casino.service;


import com.casino.CasinoConstants;
import com.casino.event.ComplianceFlagChanged;
import com.casino.event.ComplianceLimitChanged;
import com.casino.event.ComplianceStatusChanged;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ComplianceProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishComplianceStatusChanged(ComplianceStatusChanged event) {
        kafkaTemplate.send(
                CasinoConstants.COMPLIANCE_STATUS_CHANGED,
                key(event.playerProfileId()),
                event
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishComplianceFlagChanged(ComplianceFlagChanged event) {
        kafkaTemplate.send(
                CasinoConstants.COMPLIANCE_FLAG_CHANGED,
                key(event.playerProfileId()),
                event
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishGamblingLimitChanged(ComplianceLimitChanged event) {
        kafkaTemplate.send(
                CasinoConstants.COMPLIANCE_LIMIT_CHANGED,
                key(event.playerProfileId()),
                event
        );
    }

    private String key(Long playerProfileId) {
        return String.valueOf(playerProfileId);
    }
}