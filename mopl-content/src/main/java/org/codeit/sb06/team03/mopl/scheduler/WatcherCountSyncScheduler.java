package org.codeit.sb06.team03.mopl.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.repository.ContentRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WatcherCountSyncScheduler {

    private static final String LIVE_CHATROOM_KEY_PREFIX = "watching-session:room:";

    private final StringRedisTemplate redisTemplate;
    private final ContentRepository contentRepository;

    @Scheduled(fixedDelayString = "${mopl.watcher-count.sync-delay-ms:5000}")
    @Transactional("contentTransactionManager")
    public void syncWatcherCounts() {
        try {
            // 1. Redis에 활성 세션이 존재하는 room 키 목록 조회
            Set<String> roomKeys = redisTemplate.keys(LIVE_CHATROOM_KEY_PREFIX + "*");
            Set<UUID> targetContentIds = new HashSet<>();

            if (roomKeys != null) {
                for (String key : roomKeys) {
                    try {
                        String idStr = key.substring(LIVE_CHATROOM_KEY_PREFIX.length());
                        targetContentIds.add(UUID.fromString(idStr));
                    } catch (Exception e) {
                        log.warn("Invalid room key format in Redis: {}", key);
                    }
                }
            }

            // 2. RDB에 watcherCount > 0으로 기록되어 있는 콘텐츠 ID 목록 조회 (0명으로 빠져나간 방 감지)
            List<UUID> dbNonZeroIds = contentRepository.findIdsByWatcherCountGreaterThanZero();
            if (dbNonZeroIds != null) {
                targetContentIds.addAll(dbNonZeroIds);
            }

            if (targetContentIds.isEmpty()) {
                return;
            }

            // 3. 각 contentId별로 Redis의 실시간 zCard 값을 가져와 RDB에 변경분만 원자적 UPDATE
            int updatedCount = 0;
            for (UUID contentId : targetContentIds) {
                Long count = redisTemplate.opsForZSet().zCard(LIVE_CHATROOM_KEY_PREFIX + contentId);
                long currentCount = count != null ? count : 0L;

                int affectedRows = contentRepository.updateWatcherCount(contentId, currentCount);
                if (affectedRows > 0) {
                    updatedCount++;
                }
            }

            if (updatedCount > 0) {
                log.info("[WatcherCountSyncScheduler] Synchronized watcher count for {} content(s)", updatedCount);
            }
        } catch (Exception e) {
            log.error("[WatcherCountSyncScheduler] Error occurred while syncing watcher counts: {}", e.getMessage(), e);
        }
    }
}
