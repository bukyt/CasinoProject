package com.casino.service;


import com.casino.CasinoConstants;
import com.casino.event.ComplianceProfileEvent;
import com.casino.event.ComplianceRiskLevel;
import com.casino.model.profile.ComplianceProfileRiskLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.OffsetDateTime;
import java.util.Objects;


@Component
@RequiredArgsConstructor
public class ComplianceEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishStatusChanged(
        Long playerProfileId,
        boolean selfExcluded,
        ComplianceProfileRiskLevel riskLevel
    ) {
        ComplianceProfileEvent event = new ComplianceProfileEvent(
            playerProfileId,
            selfExcluded,
            toEventRiskLevel(riskLevel),
            OffsetDateTime.now()
        );

        sendAfterCommitOrNow(event);
    }

    public void publishStatusChangedIfChanged(
        Long playerProfileId,
        boolean previousSelfExcluded,
        ComplianceProfileRiskLevel previousRiskLevel,
        boolean currentSelfExcluded,
        ComplianceProfileRiskLevel currentRiskLevel
    ) {
        boolean changed =
            previousSelfExcluded != currentSelfExcluded
                || !Objects.equals(previousRiskLevel, currentRiskLevel);

        if (!changed) {
            return;
        }

        publishStatusChanged(
            playerProfileId,
            currentSelfExcluded,
            currentRiskLevel
        );
    }

    private void sendAfterCommitOrNow(ComplianceProfileEvent event) {
        Runnable send = () -> kafkaTemplate.send(
            CasinoConstants.COMPLIANCE_EVENTS_TOPIC_NAME,
            String.valueOf(event.playerProfileId()),
            event
        );

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            send.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send.run();
                }
            }
        );
    }

    private ComplianceRiskLevel toEventRiskLevel(ComplianceProfileRiskLevel riskLevel) {
        if (riskLevel == null) {
            return ComplianceRiskLevel.UNASSESSED;
        }

        return ComplianceRiskLevel.valueOf(riskLevel.name());
    }
}