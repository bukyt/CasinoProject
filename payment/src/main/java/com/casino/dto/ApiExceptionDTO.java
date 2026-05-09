package com.casino.dto;


import java.util.Map;

public record ApiExceptionDTO(String message, String code, Map<String, String> data) {
}
