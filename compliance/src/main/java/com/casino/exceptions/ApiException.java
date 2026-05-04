package com.casino.exceptions;


import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ApiException extends RuntimeException {

    private final String code;
    private final Map<String, String> messageData;
    private final HttpStatus status;

    public ApiException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
        this.messageData = Map.of();
    }

    public ApiException(String code, String message, HttpStatus status, Map<String, String> messageData) {
        super(message);
        this.code = code;
        this.status = status;
        this.messageData = messageData;
    }


}