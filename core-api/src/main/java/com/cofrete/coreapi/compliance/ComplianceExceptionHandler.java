package com.cofrete.coreapi.compliance;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ComplianceExceptionHandler {

    @ExceptionHandler(ComplianceValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse validation(ComplianceValidationException exception, HttpServletRequest request) {
        return new ErrorResponse("VALIDATION_ERROR", exception.getMessage(), List.of(), correlationId(request));
    }

    private static String correlationId(HttpServletRequest request) {
        var value = request.getHeader("X-Correlation-Id");
        return value == null || value.isBlank() ? null : value;
    }

    record ErrorResponse(String error, String message, List<String> details, String correlationId) {
    }
}
