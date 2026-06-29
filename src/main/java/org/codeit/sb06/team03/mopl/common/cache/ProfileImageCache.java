package org.codeit.sb06.team03.mopl.common.cache;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.application.in.GetAccountUseCase;
import org.codeit.sb06.team03.mopl.profile.infra.in.UserDto;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProfileImageCache {

    private final StringRedisTemplate redisTemplate;
    private final GetAccountUseCase getAccountUseCase;

    private static final String CACHE_KEY_PREFIX = "mopl:profile:image-url:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(50); // S3 URL signature duration is 1 hour, cache for 50 min

    public String getProfileImageUrl(UUID userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;
        String cachedUrl = redisTemplate.opsForValue().get(cacheKey);

        if (cachedUrl != null) {
            return cachedUrl;
        }

        // Cache miss: load latest profile from DB, generate fresh presigned URL
        UserDto userDto = getAccountUseCase.get(userId);
        String profileImageUrl = userDto.profileImageUrl();

        if (profileImageUrl != null) {
            redisTemplate.opsForValue().set(cacheKey, profileImageUrl, CACHE_TTL);
        }

        return profileImageUrl;
    }

    public void evictProfileImageUrl(UUID userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;
        redisTemplate.delete(cacheKey);
    }
}
