package com.casino.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class BadRequestException extends ApiException {
    public BadRequestException(String code, String message) {
        super(code, message, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String code, String message, Map<String, String> messageData) {
        super(code, message, HttpStatus.BAD_REQUEST, messageData);
    }
}
