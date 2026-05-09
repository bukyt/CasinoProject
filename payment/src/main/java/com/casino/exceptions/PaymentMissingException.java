package com.casino.exceptions;

import java.util.Map;

import static com.casino.exceptions.CommonApiErrorCodes.PAYMENT_MISSING;

public class PaymentMissingException extends NotFoundException {

    public static final String EXAMPLE = """
        {
          "code": "payment.missing",
          "message": "Payment with id 1 is missing",
          "data": {
            "paymentId": "1"
          }
        }
        """;

    public PaymentMissingException(Long paymentId) {
        super(
            PAYMENT_MISSING,
            "Payment with id " + paymentId + " is missing",
            Map.of("paymentId", paymentId.toString())
        );
    }
}