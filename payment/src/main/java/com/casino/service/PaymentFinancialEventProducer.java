package com.casino.service;

import com.casino.CasinoConstants;
import com.casino.event.AbstractPlayerFinancialEvent;
import com.casino.event.DepositCompleted;
import com.casino.event.WithdrawalProcessed;
import com.casino.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class PaymentFinancialEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishDepositCompleted(Payment payment) {
        DepositCompleted event = new DepositCompleted();
        populate(event, payment);
        sendAfterCommitOrNow(payment.getPlayerProfileId(), event);
    }

    public void publishWithdrawalProcessed(Payment payment) {
        WithdrawalProcessed event = new WithdrawalProcessed();
        populate(event, payment);
        sendAfterCommitOrNow(payment.getPlayerProfileId(), event);
    }

    private void populate(AbstractPlayerFinancialEvent event, Payment payment) {
        event.setPlayerProfileId(Math.toIntExact(payment.getPlayerProfileId()));
        event.setAmount(payment.getAmount());
    }

    private void sendAfterCommitOrNow(
        Long playerProfileId,
        AbstractPlayerFinancialEvent event
    ) {
        Runnable send = () -> kafkaTemplate.send(
            CasinoConstants.FINANCIAL_EVENTS_TOPIC_NAME,
            String.valueOf(playerProfileId),
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
}