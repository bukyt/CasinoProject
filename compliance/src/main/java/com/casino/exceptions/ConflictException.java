package com.casino.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ConflictException extends ApiException {

    public ConflictException(String code, String message) {
        super(code, message, HttpStatus.CONFLICT);
    }

    public ConflictException(String code, String message, Map<String, String> messageData) {
        super(code, message, HttpStatus.CONFLICT, messageData);
    }

}
