package org.codeit.sb06.team03.mopl.cache;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.service.ProfileQueryService;
import org.codeit.sb06.team03.mopl.entity.Profile;
import org.codeit.sb06.team03.mopl.service.cqrs.ExternalImageQueryService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProfileImageCache {

    private final StringRedisTemplate redisTemplate;
    private final ExternalImageQueryService imageQueryService;
    private final ProfileQueryService profileQueryService;

    private static final String CACHE_KEY_PREFIX = "mopl:profile:image-url:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(50); // S3 URL signature duration is 1 hour, cache for 50 min

    public String getProfileImageUrl(UUID userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;
        String cachedUrl = redisTemplate.opsForValue().get(cacheKey);

        if (cachedUrl != null) {
            return cachedUrl;
        }

        // Cache miss: getByIdsIn latest profile from DB, generate fresh presigned URL
        Profile profile = profileQueryService.getById(userId);
        String profileImageUrl = imageQueryService.getPresignedUrl(profile.getImageKey());

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
