package org.codeit.sb06.team03.mopl.security.jwt.registry;

import org.codeit.sb06.team03.mopl.security.jwt.*;
import org.codeit.sb06.team03.mopl.security.jwt.exception.InvalidTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Component
public class RedisJwtRegistry implements JwtRegistry {

    private final int MAX_SESSION;
    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String REFRESH_KEY_PREFIX = "token:refresh:";
    private static final String ACCESS_KEY_PREFIX = "token:access:";
    private static final String USER_SESSIONS_PREFIX = "token:user:";

    public RedisJwtRegistry(
            @Value("${mopl.jwt.max-session}") int maxSession,
            StringRedisTemplate redisTemplate,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.MAX_SESSION = maxSession;
        this.redisTemplate = redisTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public TokenPair register(JwtClaims jwtClaims) {
        TokenResult refreshToken = jwtTokenProvider.generateRefreshToken(jwtClaims);
        TokenResult accessToken = jwtTokenProvider.generateAccessToken(jwtClaims);

        String userIdStr = jwtClaims.id().toString();
        String userSessionsKey = USER_SESSIONS_PREFIX + userIdStr;

        // 세션 개수 초과 시 가장 오래된 세션 만료 처리 (MAX_SESSION 제어)
        Set<String> existingRefreshIds = redisTemplate.opsForSet().members(userSessionsKey);
        if (existingRefreshIds != null && existingRefreshIds.size() >= MAX_SESSION) {
            for (String oldRefreshId : existingRefreshIds) {
                invalidateByRefreshTokenId(oldRefreshId, userIdStr);
            }
        }

        String refreshIdStr = refreshToken.id().toString();
        String accessIdStr = accessToken.id().toString();

        long refreshTtlSec = Math.max(0, Duration.between(Instant.now(), refreshToken.expiresAt()).getSeconds());
        long accessTtlSec = Math.max(0, Duration.between(Instant.now(), accessToken.expiresAt()).getSeconds());

        // Redis 저장
        redisTemplate.opsForValue().set(REFRESH_KEY_PREFIX + refreshIdStr, accessIdStr + ":" + userIdStr, Duration.ofSeconds(refreshTtlSec));
        redisTemplate.opsForValue().set(ACCESS_KEY_PREFIX + accessIdStr, userIdStr, Duration.ofSeconds(accessTtlSec));
        redisTemplate.opsForSet().add(userSessionsKey, refreshIdStr);

        return new TokenPair(refreshToken.token(), accessToken.token());
    }

    @Override
    public boolean hasActiveAccessToken(String accessToken) {
        if (!jwtTokenProvider.validateAccessToken(accessToken)) {
            return false;
        }
        UUID accessTokenId = jwtTokenProvider.getTokenId(accessToken);
        return Boolean.TRUE.equals(redisTemplate.hasKey(ACCESS_KEY_PREFIX + accessTokenId));
    }

    @Override
    public boolean hasActiveRefreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            return false;
        }
        UUID refreshTokenId = jwtTokenProvider.getTokenId(refreshToken);
        return Boolean.TRUE.equals(redisTemplate.hasKey(REFRESH_KEY_PREFIX + refreshTokenId));
    }

    @Override
    public void invalidateAll(JwtClaims jwtClaims) {
        String userIdStr = jwtClaims.id().toString();
        String userSessionsKey = USER_SESSIONS_PREFIX + userIdStr;
        Set<String> refreshIds = redisTemplate.opsForSet().members(userSessionsKey);
        if (refreshIds != null) {
            for (String refreshId : refreshIds) {
                invalidateByRefreshTokenId(refreshId, userIdStr);
            }
        }
        redisTemplate.delete(userSessionsKey);
    }

    @Override
    public void invalidateToken(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            return;
        }
        UUID refreshTokenId = jwtTokenProvider.getTokenId(refreshToken);
        JwtClaims claims = jwtTokenProvider.getClaims(refreshToken);
        invalidateByRefreshTokenId(refreshTokenId.toString(), claims.id().toString());
    }

    @Override
    public TokenPair rotate(String oldRefreshToken) {
        if (!hasActiveRefreshToken(oldRefreshToken)) {
            throw new InvalidTokenException();
        }

        invalidateToken(oldRefreshToken);
        JwtClaims jwtClaims = jwtTokenProvider.getClaims(oldRefreshToken);
        return register(jwtClaims);
    }

    @Override
    public void clearExpiredTokenSession() {
        // Redis는 TTL에 의해 자동으로 데이터가 삭제되므로 별도의 Cleanup이 필요 없습니다.
    }

    private void invalidateByRefreshTokenId(String refreshIdStr, String userIdStr) {
        String value = redisTemplate.opsForValue().get(REFRESH_KEY_PREFIX + refreshIdStr);
        if (value != null) {
            String[] parts = value.split(":");
            if (parts.length > 0) {
                String accessIdStr = parts[0];
                redisTemplate.delete(ACCESS_KEY_PREFIX + accessIdStr);
            }
        }
        redisTemplate.delete(REFRESH_KEY_PREFIX + refreshIdStr);
        redisTemplate.opsForSet().remove(USER_SESSIONS_PREFIX + userIdStr, refreshIdStr);
    }
}
