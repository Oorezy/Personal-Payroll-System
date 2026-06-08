package com.introtech.authenticationservice.service;

import com.introtech.authenticationservice.UserDetailsImpl;
import com.introtech.authenticationservice.entity.RefreshTokens;
import com.introtech.authenticationservice.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public boolean isRefreshTokenValid(UUID jti) {
        return refreshTokenRepository.findByJti(jti).filter(
                token -> !token.isRevoked()).isPresent();
    }

    @Transactional
    public RefreshTokens generateRefreshToken(UserDetailsImpl userDetails, UUID oldJti, Duration duration) {

        RefreshTokens refreshToken = new RefreshTokens();
        refreshToken.setUserId(userDetails.getId());
        refreshToken.setExpiresAt(Instant.now().plus(duration));
        if (oldJti != null)
            refreshTokenRepository.updateLastUsed(oldJti, refreshToken.getJti());
        return refreshTokenRepository.save(refreshToken);
    }
}
