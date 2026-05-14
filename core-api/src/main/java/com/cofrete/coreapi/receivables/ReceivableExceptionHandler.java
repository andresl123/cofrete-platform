package com.cofrete.coreapi.receivables;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ReceivablesModule.class)
class ReceivableExceptionHandler {

    @ExceptionHandler(ReceivableValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse validation(ReceivableValidationException exception, HttpServletRequest request) {
        return simple("VALIDATION_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(ReceivableNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse notFound(ReceivableNotFoundException exception, HttpServletRequest request) {
        return simple("NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(ReceivableConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse conflict(ReceivableConflictException exception, HttpServletRequest request) {
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
