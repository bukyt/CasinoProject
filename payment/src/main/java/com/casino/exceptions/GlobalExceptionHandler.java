package com.casino.exceptions;

import com.casino.dto.ApiExceptionDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.casino.exceptions.CommonApiErrorCodes.*;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiExceptionDTO> handleApiException(ApiException e, HttpServletRequest request) {
        log.error("ApiException: {}", e.getMessage());

        val exception = new ApiExceptionDTO(
                e.getMessage(), e.getCode(), e.getMessageData()
        );

        return new ResponseEntity<>(exception, e.getStatus());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiExceptionDTO> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        for (ObjectError objectError : e.getBindingResult().getGlobalErrors()) {
            errors.put(
                    objectError.getObjectName(),
                    objectError.getDefaultMessage()
            );
        }

        val exception = new ApiExceptionDTO(
                "Validation failed",
                VALIDATION_FAILED,
                errors
        );

        return new ResponseEntity<>(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiExceptionDTO> handleConstraintViolationException(
            ConstraintViolationException e,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        e.getConstraintViolations().forEach(violation -> {
            String property = violation.getPropertyPath().toString();
            errors.put(property, violation.getMessage());
        });

        val exception = new ApiExceptionDTO(
                "Validation failed",
                VALIDATION_FAILED,
                errors
        );

        return new ResponseEntity<>(exception, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiExceptionDTO> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e,
            HttpServletRequest request
    ) {
        Map<String, String> data = new LinkedHashMap<>();

        Throwable cause = e.getCause();

        if (cause instanceof InvalidFormatException invalidFormatException) {
            String fieldName = "unknown";

            if (!invalidFormatException.getPath().isEmpty()) {
                var path = invalidFormatException.getPath();
                var lastReference = path.get(path.size() - 1);

                fieldName = lastReference.getDescription();
            }

            data.put("field", fieldName);
            data.put("rejectedValue", String.valueOf(invalidFormatException.getValue()));
            data.put("reason", "Invalid value format");
        } else {
            data.put("reason", "Malformed request body");
        }

        val exception = new ApiExceptionDTO(
                "Invalid request body",
                INVALID_REQUEST_BODY,
                data
        );

        return new ResponseEntity<>(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiExceptionDTO> handleException(Exception e, HttpServletRequest request) {
        log.error("Exception: {}", e);

        val exception = new ApiExceptionDTO(e.getMessage(), INTERNAL_ERROR, Map.of());

        return new ResponseEntity<>(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
