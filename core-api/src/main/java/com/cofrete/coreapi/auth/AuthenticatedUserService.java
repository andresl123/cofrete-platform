package com.cofrete.coreapi.auth;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticatedUserService {

    private final AppUserRepository users;

    AuthenticatedUserService(AppUserRepository users) {
        this.users = users;
    }

    @Transactional
    public AppUser requireUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authenticated principal is required.");
        }

        return users.findByPrincipalName(authentication.getName())
            .orElseGet(() -> users.save(new AppUser(authentication.getName())));
    }
}
