package com.casino.exceptions.limit;

import com.casino.exceptions.NotFoundException;

import java.util.Map;

import static com.casino.exceptions.CommonApiErrorCodes.COMPLIANCE_LIMIT_MISSING;

public class ComplianceLimitMissingException extends NotFoundException {

    public static final String EXAMPLE = """
        {
          "code": "compliance.limit.missing",
          "message": "Compliance limit with id 1 is missing",
          "data": {
            "limitId": "1"
          }
        }
        """;

    public ComplianceLimitMissingException(Long limitId) {
        super(
                COMPLIANCE_LIMIT_MISSING,
                "Compliance limit with id " + limitId + " is missing",
                Map.of("limitId", limitId.toString())
        );
    }
}