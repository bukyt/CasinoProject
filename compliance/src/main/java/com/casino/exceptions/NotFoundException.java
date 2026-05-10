package com.casino.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class NotFoundException extends ApiException {
    public NotFoundException(String code, String message) {
        super(code, message, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String code, String message, Map<String, String> messageData) {
        super(code, message, HttpStatus.NOT_FOUND, messageData);
    }
}
