package org.codeit.sb06.team03.mopl.security.jwt.registry;

import org.codeit.sb06.team03.mopl.security.jwt.JwtClaims;
import org.codeit.sb06.team03.mopl.security.jwt.TokenPair;

import java.util.UUID;

public interface JwtRegistry {

    TokenPair register(JwtClaims jwtClaims);

    boolean hasActiveRefreshToken(String refreshToken);

    void invalidateAllByUserId(UUID userId);

    void invalidateToken(String refreshToken);

    TokenPair rotate(String oldRefreshToken);

    void clearExpiredTokenSession();
}
