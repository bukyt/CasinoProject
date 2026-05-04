package com.casino.exceptions.flag;

import com.casino.exceptions.NotFoundException;

import java.util.Map;

import static com.casino.exceptions.CommonApiErrorCodes.COMPLIANCE_FLAG_MISSING;

public class ComplianceFlagMissingException extends NotFoundException {

    public static final String EXAMPLE = """
        {
          "code": "compliance.flag.missing",
          "message": "Compliance flag with id 1 is missing",
          "data": {
            "flagId": "1"
          }
        }
        """;

    public ComplianceFlagMissingException(Long flagId) {
        super(
                COMPLIANCE_FLAG_MISSING,
                "Compliance flag with id " + flagId + " is missing",
                Map.of("flagId", flagId.toString())
        );
    }
}