package org.codeit.sb06.team03.mopl.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.service.application.InboxService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class InboxCleanupScheduler {

    private final InboxService inboxService;

    // 7일 이상 지난 Inbox 이벤트 정리 (매일 새벽 3시 30분 실행)
    @Scheduled(cron = "0 30 3 * * *")
    public void purgeOldInboxEvents() {
        Instant threshold = Instant.now().minus(Duration.ofDays(7));
        int purgedCount = inboxService.purgeOldInboxEvents(threshold);
        if (purgedCount > 0) {
            log.info("[Inbox Cleanup] Successfully purged {} inbox events older than 7 days.", purgedCount);
        }
    }
}
