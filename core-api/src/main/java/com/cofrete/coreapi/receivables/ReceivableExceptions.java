package com.cofrete.coreapi.receivables;

class ReceivableValidationException extends RuntimeException {
    ReceivableValidationException(String message) {
        super(message);
    }
}

class ReceivableNotFoundException extends RuntimeException {
    ReceivableNotFoundException(String message) {
        super(message);
    }
}

class ReceivableConflictException extends RuntimeException {
    ReceivableConflictException(String message) {
        super(message);
    }
}
