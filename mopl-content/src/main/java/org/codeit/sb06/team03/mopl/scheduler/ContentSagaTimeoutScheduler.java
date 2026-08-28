package org.codeit.sb06.team03.mopl.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.entity.Content;
import org.codeit.sb06.team03.mopl.enums.ContentStatus;
import org.codeit.sb06.team03.mopl.repository.ContentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentSagaTimeoutScheduler {

    private final ContentRepository contentRepository;

    // Saga 대기 타임아웃 임계치 (3분)
    private static final Duration TIMEOUT_DURATION = Duration.ofMinutes(3);

    @Scheduled(fixedDelay = 60000) // 1분 주기 실행
    @Transactional("contentTransactionManager")
    public void handleTimedOutSaga() {
        Instant threshold = Instant.now().minus(TIMEOUT_DURATION);
        List<Content> timedOutContents = contentRepository.findAllByStatusAndUpdatedAtBefore(ContentStatus.DELETING, threshold);

        if (!timedOutContents.isEmpty()) {
            log.warn("Found {} timed out deleting contents. Executing automatic compensation rollback.", timedOutContents.size());
            for (Content content : timedOutContents) {
                log.warn("Saga timeout reached for contentId: {}. Restoring status to ACTIVE.", content.getId());
                content.restoreActive();
                contentRepository.save(content);
            }
        }
    }
}
