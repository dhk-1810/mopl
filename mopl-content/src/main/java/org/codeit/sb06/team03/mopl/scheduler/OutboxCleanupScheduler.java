package org.codeit.sb06.team03.mopl.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.service.application.OutboxService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxCleanupScheduler {

    private final OutboxService outboxService;

    // 7일 이상 지난 PUBLISHED 완료 아웃박스 이벤트 정리 (매일 새벽 3시 실행)
    @Scheduled(cron = "0 0 3 * * *")
    public void purgeOldPublishedEvents() {
        Instant threshold = Instant.now().minus(Duration.ofDays(7));
        int purgedCount = outboxService.purgeOldPublishedEvents(threshold);
        if (purgedCount > 0) {
            log.info("[Outbox Cleanup] Successfully purged {} published outbox events older than 7 days.", purgedCount);
        }
    }
}
