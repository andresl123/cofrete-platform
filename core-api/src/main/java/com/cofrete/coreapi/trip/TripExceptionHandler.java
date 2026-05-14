package com.cofrete.coreapi.trip;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class TripExceptionHandler {

    @ExceptionHandler(TripValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse validation(TripValidationException exception, HttpServletRequest request) {
        return simple("VALIDATION_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(TripNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse notFound(TripNotFoundException exception, HttpServletRequest request) {
        return simple("NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(TripForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ErrorResponse forbidden(TripForbiddenException exception, HttpServletRequest request) {
        return simple("FORBIDDEN", exception.getMessage(), request);
    }

    @ExceptionHandler(TripConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse conflict(TripConflictException exception, HttpServletRequest request) {
        return simple("CONFLICT", exception.getMessage(), request);
    }

    @ExceptionHandler(CalculationUnavailableException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    ErrorResponse calculationUnavailable(CalculationUnavailableException exception, HttpServletRequest request) {
        return simple("CALCULATION_UNAVAILABLE", exception.getMessage(), request);
    }

    @ExceptionHandler(SnapshotPendingException.class)
    @ResponseStatus(HttpStatus.TOO_EARLY)
    ErrorResponse snapshotPending(SnapshotPendingException exception, HttpServletRequest request) {
        return simple("SNAPSHOT_PENDING", exception.getMessage(), request);
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
