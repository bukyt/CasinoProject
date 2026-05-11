package com.casino.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String code, String message) {
        super(code, message, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(String code, String message, Map<String, String> messageData) {
        super(code, message, HttpStatus.FORBIDDEN, messageData);
    }
}
