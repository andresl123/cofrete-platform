package com.cofrete.coreapi.reserve;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ReserveExceptionHandler {

    @ExceptionHandler(ReserveValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse validation(ReserveValidationException exception, HttpServletRequest request) {
        return simple("VALIDATION_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(ReserveConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse conflict(ReserveConflictException exception, HttpServletRequest request) {
        return simple("CONFLICT", exception.getMessage(), request);
    }

    private static ErrorResponse simple(String error, String message, HttpServletRequest request) {
        return new ErrorResponse(error, message, List.of(), correlationId(request));
    }

    private static String correlationId(HttpServletRequest request) {
        var value = request.getHeader("X-Correlation-Id");
        return value == null || value.isBlank() ? null : value;
    }

    record ErrorResponse(String error, String message, List<Object> details, String correlationId) {
    }
}
