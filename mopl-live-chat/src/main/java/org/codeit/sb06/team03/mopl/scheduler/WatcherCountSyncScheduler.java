package org.codeit.sb06.team03.mopl.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.client.ContentGrpcClient;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * mopl-live-chat의 Redis 시청 세션을 집계하여
 * mopl-content로 실시간 시청자 수를 gRPC 일괄 전송하는 스케줄러 (기본 주기: 1분)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WatcherCountSyncScheduler {

    private static final String LIVE_CHATROOM_KEY_PREFIX = "watching-session:room:";

    private final StringRedisTemplate redisTemplate;
    private final ContentGrpcClient contentGrpcClient;

    // 이전 동기화 시점에 시청자가 1명 이상 있었던 방 목록 (0명으로 비워진 방 감지용)
    private final Set<UUID> previousActiveRooms = ConcurrentHashMap.newKeySet();

    @Scheduled(fixedDelayString = "${MOPL_WATCHER_COUNT_SYNC_DELAY_MS:60000}")
    public void syncWatcherCounts() {
        try {
            Map<UUID, Long> currentCounts = new HashMap<>();
            Set<UUID> currentlyActiveRooms = new HashSet<>();

            ScanOptions scanOptions = ScanOptions.scanOptions()
                    .match(LIVE_CHATROOM_KEY_PREFIX + "*")
                    .count(200)
                    .build();

            try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
                while (cursor.hasNext()) {
                    String key = cursor.next();
                    try {
                        String idStr = key.substring(LIVE_CHATROOM_KEY_PREFIX.length());
                        UUID contentId = UUID.fromString(idStr);

                        Long count = redisTemplate.opsForZSet().zCard(key);
                        long currentCount = count != null ? count : 0L;

                        currentCounts.put(contentId, currentCount);
                        if (currentCount > 0) {
                            currentlyActiveRooms.add(contentId);
                        }
                    } catch (Exception e) {
                        log.warn("Invalid room key format in Redis: {}", key);
                    }
                }
            }

            // 이전에 활성화되었으나 이번 스캔에서 0명이 되었거나 세션 키가 삭제된 방 감지 -> 0명으로 반영
            for (UUID prevId : previousActiveRooms) {
                if (!currentlyActiveRooms.contains(prevId)) {
                    currentCounts.put(prevId, 0L);
                }
            }

            if (currentCounts.isEmpty()) {
                return;
            }

            int updated = contentGrpcClient.batchUpdateWatcherCounts(currentCounts);
            if (updated > 0) {
                log.info("[WatcherCountSyncScheduler] Synchronized watcher count for {} content(s) via gRPC", updated);
            }

            previousActiveRooms.clear();
            previousActiveRooms.addAll(currentlyActiveRooms);

        } catch (Exception e) {
            log.error("[WatcherCountSyncScheduler] Error occurred while syncing watcher counts: {}", e.getMessage(), e);
        }
    }
}
