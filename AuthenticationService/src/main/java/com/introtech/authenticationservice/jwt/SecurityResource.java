package com.introtech.authenticationservice.jwt;

public interface SecurityResource {

    String JWT_TYPE_CLAIM = "typ";

    String ACCESS_TOKEN_TYPE = "access-token";

    String REFRESH_TOKEN_TYPE = "refresh-token";

    String ROLE_CLAIM = "roles";

    String ROLE_PREFIX = "ROLE_";
}
