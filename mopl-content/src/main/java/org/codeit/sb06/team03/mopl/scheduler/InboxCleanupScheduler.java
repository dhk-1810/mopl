package org.codeit.sb06.team03.mopl.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.service.application.InboxService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@ConditionalOnExpression("'${mopl.saga.type:grpc}' == 'cdc-kafka'")
@RequiredArgsConstructor
public class InboxCleanupScheduler {

    private final InboxService inboxService;

    // 보관 주기: 7일
    private static final Duration RETENTION_PERIOD = Duration.ofDays(7);

    @Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시 실행
    public void cleanupOldInboxEvents() {
        log.info("[Inbox Cleanup] Starting scheduled cleanup for processed inbox events older than 7 days...");
        Instant threshold = Instant.now().minus(RETENTION_PERIOD);

        try {
            int deletedCount = inboxService.purgeOldInboxEvents(threshold);
            log.info("[Inbox Cleanup] Successfully purged {} old inbox event(s)", deletedCount);
        } catch (Exception e) {
            log.error("[Inbox Cleanup] Failed to purge old inbox events: {}", e.getMessage(), e);
        }
    }
}
