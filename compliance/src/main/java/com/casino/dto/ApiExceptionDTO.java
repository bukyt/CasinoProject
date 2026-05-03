package com.casino.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Map;

@Data
@AllArgsConstructor
public class ApiExceptionDTO {
    private final String message;
    private final String code;

    private final Map<String, String> data;
}
