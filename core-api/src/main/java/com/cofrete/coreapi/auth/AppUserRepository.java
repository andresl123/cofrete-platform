package com.cofrete.coreapi.auth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface AppUserRepository extends JpaRepository<AppUser, String> {

    Optional<AppUser> findByPrincipalName(String principalName);
}
