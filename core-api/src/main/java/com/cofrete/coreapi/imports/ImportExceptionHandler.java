package com.cofrete.coreapi.imports;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ImportsModule.class)
class ImportExceptionHandler {

    @ExceptionHandler(ImportDataNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse notFound(ImportDataNotFoundException ex, HttpServletRequest request) {
        return simple("NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse validation(IllegalArgumentException ex, HttpServletRequest request) {
        return simple("VALIDATION_ERROR", ex.getMessage(), request);
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
