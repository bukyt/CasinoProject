package com.casino.exceptions;

import com.casino.dto.ApiExceptionDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

import static com.casino.exceptions.CommonApiErrorCodes.INTERNAL_ERROR;

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiExceptionDTO> handleException(Exception e, HttpServletRequest request) {
        log.error("Exception: {}", e);

        val exception = new ApiExceptionDTO(e.getMessage(), INTERNAL_ERROR, Map.of());

        return new ResponseEntity<>(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
