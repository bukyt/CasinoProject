package com.casino.exceptions;

public class CommonApiErrorCodes {


    public static final String INVALID_REQUEST_BODY = "client.invalid_request_body";
    public static final String VALIDATION_FAILED = "client.validation_failed";

    public static final String COMPLIANCE_PROFILE_EXISTS = "compliance.profile.exists";
    public static final String COMPLIANCE_PROFILE_MISSING = "compliance.profile.missing";
    public static final String INTERNAL_ERROR = "server.internal_error";

    public static final String COMPLIANCE_LIMIT_EXISTS = "compliance.limit.exists";
    public static final String COMPLIANCE_LIMIT_MISSING = "compliance.limit.missing";
    public static final String INVALID_COMPLIANCE_LIMIT = "compliance.limit.invalid";

    public static final String COMPLIANCE_FLAG_EXISTS = "compliance.flag.exists";
    public static final String COMPLIANCE_FLAG_MISSING = "compliance.flag.missing";
    public static final String INVALID_COMPLIANCE_FLAG = "compliance.flag.invalid";

}
