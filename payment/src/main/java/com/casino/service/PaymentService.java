package com.casino.service;

import com.casino.dto.CreatePaymentDto;
import com.casino.dto.PaymentDto;
import com.casino.dto.PaymentProviderWebhookDto;
import com.casino.exceptions.PaymentMissingException;
import com.casino.model.Payment;
import com.casino.model.PaymentStatus;
import com.casino.model.PaymentType;
import com.casino.providermock.MockPaymentProviderClient;
import com.casino.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MockPaymentProviderClient mockPaymentProviderClient;
    private final PaymentWalletValidator paymentWalletValidator;
    private final WalletClient walletClient;
    private final PaymentFinancialEventProducer paymentFinancialEventProducer;

    public PaymentDto createPayment(CreatePaymentDto request, PaymentType type) {
        paymentWalletValidator.validatePaymentAllowed(request, type);

        Payment payment = Payment.builder()
            .playerProfileId(request.playerProfileId())
            .type(type)
            .amount(request.amount())
            .provider(request.provider())
            .status(PaymentStatus.PENDING)
            .createdDate(OffsetDateTime.now())
            .build();

        Payment savedPayment = paymentRepository.save(payment);

        mockPaymentProviderClient.simulateProviderCallback(
            savedPayment.getPaymentId(),
            savedPayment.getProvider()
        );

        return toDto(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new PaymentMissingException(id));

        return toDto(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsByPlayerId(Long playerId) {
        return paymentRepository.findByPlayerProfileId(playerId)
            .stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional
    public void handleProviderWebhook(PaymentProviderWebhookDto webhookDto) {
        Payment payment = paymentRepository.findById(webhookDto.paymentId())
            .orElseThrow(() -> new PaymentMissingException(webhookDto.paymentId()));

        PaymentStatus oldStatus = payment.getStatus();
        PaymentStatus newStatus = webhookDto.status();

        boolean completedNow = shouldApplyWalletMutation(oldStatus, newStatus);

        if (completedNow) {
            applyWalletMutation(payment);
        }

        payment.setStatus(newStatus);
        Payment savedPayment = paymentRepository.save(payment);

        if (completedNow) {
            switch (savedPayment.getType()) {
                case DEPOSIT -> paymentFinancialEventProducer.publishDepositCompleted(savedPayment);
                case WITHDRAWAL -> paymentFinancialEventProducer.publishWithdrawalProcessed(savedPayment);
                default -> throw new IllegalArgumentException(
                    "Unsupported payment type: " + savedPayment.getType()
                );
            }
        }
    }

    private boolean shouldApplyWalletMutation(
        PaymentStatus oldStatus,
        PaymentStatus newStatus
    ) {
        return oldStatus != PaymentStatus.COMPLETED
            && newStatus == PaymentStatus.COMPLETED;
    }

    private void applyWalletMutation(Payment payment) {
        switch (payment.getType()) {
            case DEPOSIT -> walletClient.debit(
                payment.getPlayerProfileId(),
                payment.getAmount()
            );
            case WITHDRAWAL -> walletClient.credit(
                payment.getPlayerProfileId(),
                payment.getAmount()
            );
            default -> throw new IllegalArgumentException(
                "Unsupported payment type: " + payment.getType()
            );
        }
    }

    private PaymentDto toDto(Payment payment) {
        return new PaymentDto(
            payment.getPaymentId(),
            payment.getPlayerProfileId(),
            payment.getType(),
            payment.getAmount(),
            payment.getProvider(),
            payment.getStatus(),
            payment.getCreatedDate()
        );
    }
}