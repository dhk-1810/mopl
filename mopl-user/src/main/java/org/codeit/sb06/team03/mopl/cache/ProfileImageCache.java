package org.codeit.sb06.team03.mopl.cache;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.application.in.GetAccountUseCase;
import org.codeit.sb06.team03.mopl.image.application.in.GetPresignedUrlUseCase;
import org.codeit.sb06.team03.mopl.profile.application.in.GetProfileUseCase;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProfileImageCache {

    private final StringRedisTemplate redisTemplate;
    private final GetAccountUseCase getAccountUseCase;
    private final GetPresignedUrlUseCase getPresignedUrlUseCase;

    private static final String CACHE_KEY_PREFIX = "mopl:profile:image-url:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(50); // S3 URL signature duration is 1 hour, cache for 50 min
    private final GetProfileUseCase getProfileUseCase;

    public String getProfileImageUrl(UUID userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;
        String cachedUrl = redisTemplate.opsForValue().get(cacheKey);

        if (cachedUrl != null) {
            return cachedUrl;
        }

        // Cache miss: getByIdsIn latest profile from DB, generate fresh presigned URL
        Profile profile = getProfileUseCase.getById(userId);
        String profileImageUrl = getPresignedUrlUseCase.getPresignedUrl(profile.getImageKey());

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
