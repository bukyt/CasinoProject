package com.casino.exceptions;

import com.casino.dto.PaymentNotAllowedReason;
import com.casino.model.PaymentType;

import java.util.Map;

import static com.casino.exceptions.CommonApiErrorCodes.PAYMENT_NOT_ALLOWED;

public class PaymentNotAllowedException extends ForbiddenException {

    public static final String EXAMPLE = """
        {
          "code": "payment.not.allowed",
          "message": "Payment of type WITHDRAWAL is not allowed for player 123",
          "data": {
            "playerProfileId": "123",
            "paymentType": "WITHDRAWAL",
            "reason": "WITHDRAWAL_NOT_ALLOWED"
          }
        }
        """;

    public PaymentNotAllowedException(
        Long playerProfileId,
        PaymentType paymentType,
        PaymentNotAllowedReason reason
    ) {
        super(
            PAYMENT_NOT_ALLOWED,
            "Payment of type " + paymentType + " is not allowed for player " + playerProfileId,
            Map.of(
                "playerProfileId", playerProfileId.toString(),
                "paymentType", paymentType.name(),
                "reason", reason.name()
            )
        );
    }
}
