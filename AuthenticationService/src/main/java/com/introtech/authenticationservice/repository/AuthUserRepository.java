package com.introtech.authenticationservice.repository;

import com.introtech.authenticationservice.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailAndClient_ClientName(String email, String clientClientName);

    boolean existsByEmailIgnoreCaseAndVerified(String email, boolean verified);
}
