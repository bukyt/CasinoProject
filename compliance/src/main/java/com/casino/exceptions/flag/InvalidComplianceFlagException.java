package com.casino.exceptions.flag;

import com.casino.exceptions.BadRequestException;

import java.util.Map;

import static com.casino.exceptions.CommonApiErrorCodes.INVALID_COMPLIANCE_FLAG;

public class InvalidComplianceFlagException extends BadRequestException {

    public static final String EXAMPLE = """
        {
          "code": "compliance.flag.invalid",
          "message": "Resolved date cannot be before created date",
          "data": {
            "reason": "Resolved date cannot be before created date"
          }
        }
        """;

    public InvalidComplianceFlagException(String reason) {
        super(
            INVALID_COMPLIANCE_FLAG,
            reason,
            Map.of("reason", reason)
        );
    }
}