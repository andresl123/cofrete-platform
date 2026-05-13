package com.cofrete.coreapi.profile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse validationError(MethodArgumentNotValidException exception, HttpServletRequest request) {
        var details = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> new ErrorDetail(error.getField(), "INVALID_FIELD", error.getDefaultMessage()))
            .toList();
        return new ErrorResponse("VALIDATION_ERROR", "Request contains invalid fields.", details, correlationId(request));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse unreadable(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return new ErrorResponse("VALIDATION_ERROR", "Request body is invalid.", List.of(), correlationId(request));
    }

    @ExceptionHandler(ProfileConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse conflict(ProfileConflictException exception, HttpServletRequest request) {
        return simple("CONFLICT", exception.getMessage(), request);
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse notFound(ProfileNotFoundException exception, HttpServletRequest request) {
        return simple("NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(ProfileForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ErrorResponse forbidden(ProfileForbiddenException exception, HttpServletRequest request) {
        return simple("FORBIDDEN", exception.getMessage(), request);
    }

    private static ErrorResponse simple(String error, String message, HttpServletRequest request) {
        return new ErrorResponse(error, message, List.of(), correlationId(request));
    }

    private static String correlationId(HttpServletRequest request) {
        var value = request.getHeader("X-Correlation-Id");
        return value == null || value.isBlank() ? null : value;
    }

    record ErrorResponse(String error, String message, List<ErrorDetail> details, String correlationId) {
    }

    record ErrorDetail(String field, String code, String message) {
    }
}
