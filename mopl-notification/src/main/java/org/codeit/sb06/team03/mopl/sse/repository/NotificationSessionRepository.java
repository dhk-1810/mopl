package org.codeit.sb06.team03.mopl.sse.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * 사용자가 지금 몇 번 알림 서버에 접속해 있는지 알려주는 라우팅 주소록. (Redis)
 * 샤딩 환경에서는 각 Redis 서버가 각자의 라우팅 정보를 가짐.
 * 샤딩 키로는 userId를 해시 계산해 사용.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationSessionRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String SESSION_KEY_PREFIX = "notification:session:";
    private static final Duration SESSION_TTL = Duration.ofHours(2);

    private String getKey(UUID userId) {
        return SESSION_KEY_PREFIX + "{" + userId + "}";
    }

    public void addSession(UUID userId, String instanceId) {
        String key = getKey(userId);
        try {
            redisTemplate.opsForSet().add(key, instanceId);
            redisTemplate.expire(key, SESSION_TTL);
        } catch (Exception e) {
            log.error("Failed to add notification session to Redis for user: {}", userId, e);
        }
    }

    public void removeSession(UUID userId, String instanceId) {
        String key = getKey(userId);
        try {
            redisTemplate.opsForSet().remove(key, instanceId);
            Long remaining = redisTemplate.opsForSet().size(key);
            if (remaining != null && remaining == 0) {
                redisTemplate.delete(key);
            }
        } catch (Exception e) {
            log.error("Failed to remove notification session from Redis for user: {}", userId, e);
        }
    }

    public Set<String> findInstanceIds(UUID userId) {
        String key = getKey(userId);
        try {
            Set<String> members = redisTemplate.opsForSet().members(key);
            return members != null ? members : Collections.emptySet();
        } catch (Exception e) {
            log.error("Failed to find notification sessions from Redis for user: {}", userId, e);
            return Collections.emptySet();
        }
    }

    public void refreshSessionTtl(UUID userId) {
        String key = getKey(userId);
        try {
            redisTemplate.expire(key, SESSION_TTL);
        } catch (Exception e) {
            log.error("Failed to refresh notification session TTL for user: {}", userId, e);
        }
    }
}
