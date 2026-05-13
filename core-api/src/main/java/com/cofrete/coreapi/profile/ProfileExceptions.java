package com.cofrete.coreapi.profile;

class ProfileConflictException extends RuntimeException {

    ProfileConflictException(String message) {
        super(message);
    }
}

class ProfileNotFoundException extends RuntimeException {

    ProfileNotFoundException(String message) {
        super(message);
    }
}

class ProfileForbiddenException extends RuntimeException {

    ProfileForbiddenException(String message) {
        super(message);
    }
}
