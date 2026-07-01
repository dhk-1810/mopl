package org.codeit.sb06.team03.mopl.security.jwt;

public record TokenPair(
        String refreshToken,
        String accessToken
) {
}
