package org.codeit.sb06.team03.mopl.security.jwt.registry;

import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.event.WatchingSessionDeleteRequestEvent;
import org.codeit.sb06.team03.mopl.security.jwt.*;
import org.codeit.sb06.team03.mopl.security.jwt.exception.InvalidTokenException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class RedisJwtRegistry implements JwtRegistry {

    private final int MAX_SESSION;
    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final RabbitTemplate rabbitTemplate;

    private static final String REFRESH_KEY_PREFIX = "token:refresh:";
    private static final String USER_SESSIONS_PREFIX = "token:user:";

    public RedisJwtRegistry(
            @Value("${mopl.jwt.max-session}") int maxSession,
            StringRedisTemplate redisTemplate,
            JwtTokenProvider jwtTokenProvider,
            RabbitTemplate rabbitTemplate
    ) {
        this.MAX_SESSION = maxSession;
        this.redisTemplate = redisTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public TokenPair register(JwtClaims jwtClaims) {
        TokenResult refreshToken = jwtTokenProvider.generateRefreshToken(jwtClaims);
        TokenResult accessToken = jwtTokenProvider.generateAccessToken(jwtClaims);

        String userIdStr = jwtClaims.id().toString();
        String userSessionsKey = USER_SESSIONS_PREFIX + userIdStr;

        // 세션 개수 초과 시 가장 오래된 세션 만료 처리 (FIFO 제어)
        Long currentSessionCount = redisTemplate.opsForZSet().zCard(userSessionsKey);
        if (currentSessionCount != null && currentSessionCount >= MAX_SESSION) {
            long toRemoveCount = currentSessionCount - MAX_SESSION + 1;
            Set<String> oldestRefreshIds = redisTemplate.opsForZSet().range(userSessionsKey, 0, toRemoveCount - 1);
            if (oldestRefreshIds != null && !oldestRefreshIds.isEmpty()) {
                log.info("Max session limit ({}) reached for user {}. Evicting {} oldest session(s): {}",
                        MAX_SESSION, userIdStr, oldestRefreshIds.size(), oldestRefreshIds);
                for (String oldRefreshId : oldestRefreshIds) {
                    invalidateByRefreshTokenId(oldRefreshId, userIdStr);
                }

                // 동시 로그인 대수 초과로 인한 강제 세션 만료 시 워칭 세션 정리 이벤트 발행
                rabbitTemplate.convertAndSend(
                        RabbitConfig.WS_EXCHANGE,
                        RabbitConfig.WS_DELETE_ROUTING,
                        new WatchingSessionDeleteRequestEvent(jwtClaims.id())
                );
            }
        }

        String refreshIdStr = refreshToken.id().toString();
        long refreshTtlSec = Math.max(0, Duration.between(Instant.now(), refreshToken.expiresAt()).getSeconds());

        // Redis 저장 (RefreshToken String 및 유저 세션 ZSet score=현재 타임스탬프)
        redisTemplate.opsForValue().set(REFRESH_KEY_PREFIX + refreshIdStr, userIdStr, Duration.ofSeconds(refreshTtlSec));
        redisTemplate.opsForZSet().add(userSessionsKey, refreshIdStr, (double) Instant.now().toEpochMilli());
        redisTemplate.expire(userSessionsKey, Duration.ofSeconds(refreshTtlSec));

        return new TokenPair(refreshToken.token(), accessToken.token());
    }

    @Override
    public boolean hasActiveRefreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            return false;
        }
        UUID refreshTokenId = jwtTokenProvider.getTokenId(refreshToken);
        return redisTemplate.hasKey(REFRESH_KEY_PREFIX + refreshTokenId);
    }

    @Override
    public void invalidateAllByUserId(UUID userId) {
        String userIdStr = userId.toString();
        String userSessionsKey = USER_SESSIONS_PREFIX + userIdStr;
        Set<String> refreshIds = redisTemplate.opsForZSet().range(userSessionsKey, 0, -1);
        if (refreshIds != null) {
            for (String refreshId : refreshIds) {
                redisTemplate.delete(REFRESH_KEY_PREFIX + refreshId);
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
        redisTemplate.delete(REFRESH_KEY_PREFIX + refreshIdStr);
        redisTemplate.opsForZSet().remove(USER_SESSIONS_PREFIX + userIdStr, refreshIdStr);
    }
}
