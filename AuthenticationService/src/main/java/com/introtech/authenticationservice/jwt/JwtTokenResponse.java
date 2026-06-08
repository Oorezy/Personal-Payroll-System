package com.introtech.authenticationservice.jwt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtTokenResponse {

    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private String refreshToken;
    private Long expiresIn;
}
