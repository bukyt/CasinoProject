package com.casino.service;

import com.casino.dto.CreatePaymentDto;
import com.casino.dto.PaymentNotAllowedReason;
import com.casino.dto.eligibility.EligibilityResponseDTO;
import com.casino.exceptions.PaymentNotAllowedException;
import com.casino.model.PaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentComplianceValidator {

    private final ComplianceClient complianceClient;

    public void validatePaymentAllowed(CreatePaymentDto request, PaymentType type) {
        EligibilityResponseDTO eligibility =
            complianceClient.getEligibility(request.playerProfileId());

        switch (type) {
            case WITHDRAWAL -> validateWithdrawal(request, eligibility);
            case DEPOSIT -> validateDeposit(request, eligibility);
            default -> throw new IllegalArgumentException("Unsupported payment type: " + type);
        }
    }

    private void validateWithdrawal(
        CreatePaymentDto request,
        EligibilityResponseDTO eligibility
    ) {
        if (!eligibility.mayWithdraw()) {
            throw new PaymentNotAllowedException(
                request.playerProfileId(),
                PaymentType.WITHDRAWAL,
                PaymentNotAllowedReason.WITHDRAWAL_NOT_ALLOWED
            );
        }

        if (eligibility.activeWithdrawalLimit() != null
            && request.amount().compareTo(BigDecimal.valueOf(
            eligibility.activeWithdrawalLimit().amount()
        )) > 0) {
            throw new PaymentNotAllowedException(
                request.playerProfileId(),
                PaymentType.WITHDRAWAL,
                PaymentNotAllowedReason.WITHDRAWAL_LIMIT_EXCEEDED
            );
        }
    }

    private void validateDeposit(
        CreatePaymentDto request,
        EligibilityResponseDTO eligibility
    ) {
        if (!eligibility.mayBet()) {
            throw new PaymentNotAllowedException(
                request.playerProfileId(),
                PaymentType.DEPOSIT,
                PaymentNotAllowedReason.DEPOSIT_NOT_ALLOWED
            );
        }

        if (eligibility.activeBetLimit() != null
            && request.amount().compareTo(BigDecimal.valueOf(
            eligibility.activeBetLimit().amount()
        )) > 0) {
            throw new PaymentNotAllowedException(
                request.playerProfileId(),
                PaymentType.DEPOSIT,
                PaymentNotAllowedReason.DEPOSIT_LIMIT_EXCEEDED
            );
        }
    }
}