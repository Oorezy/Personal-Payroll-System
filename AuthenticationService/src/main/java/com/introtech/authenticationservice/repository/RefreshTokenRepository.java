package com.introtech.authenticationservice.repository;

import com.introtech.authenticationservice.entity.RefreshTokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokens, Long> {

    @Modifying
    @Query("UPDATE RefreshTokens rt SET rt.revoked = true, rt.replacedByJti = :newJti WHERE rt.jti = :oldJti")
    void updateLastUsed(UUID oldJti, UUID newJti);

    Optional<RefreshTokens> findByJti(UUID jti);
}