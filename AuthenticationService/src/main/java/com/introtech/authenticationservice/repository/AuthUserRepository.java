package com.introtech.authenticationservice.repository;

import com.introtech.authenticationservice.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    AuthUser findByEmailIgnoreCase(String email);

    boolean existsByEmailAndClient_ClientName(String email, String clientClientName);
}
