package com.cofrete.coreapi.reserve;

class ReserveValidationException extends RuntimeException {

    ReserveValidationException(String message) {
        super(message);
    }
}

class ReserveConflictException extends RuntimeException {

    ReserveConflictException(String message) {
        super(message);
    }
}
