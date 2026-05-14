package com.cofrete.coreapi.trip;

class TripValidationException extends RuntimeException {

    TripValidationException(String message) {
        super(message);
    }
}

class TripNotFoundException extends RuntimeException {

    TripNotFoundException(String message) {
        super(message);
    }
}

class TripForbiddenException extends RuntimeException {

    TripForbiddenException(String message) {
        super(message);
    }
}

class TripConflictException extends RuntimeException {

    TripConflictException(String message) {
        super(message);
    }
}

class CalculationUnavailableException extends RuntimeException {

    CalculationUnavailableException(String message) {
        super(message);
    }
}

class SnapshotPendingException extends RuntimeException {

    SnapshotPendingException(String message) {
        super(message);
    }
}
