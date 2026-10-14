package org.codeit.sb06.team03.mopl.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.repository.CurationRepository;
import org.codeit.sb06.team03.mopl.repository.PlaylistRepository;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalContentViewRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaylistPurgeScheduler {

    private final CurationRepository curationRepository;
    private final PlaylistRepository playlistRepository;
    private final ExternalContentViewRepository externalContentViewRepository;

    // 보관 주기: 7일
    private static final Duration RETENTION_PERIOD = Duration.ofDays(7);

    @Scheduled(cron = "0 0 4 * * ?") // 매일 새벽 4시 실행
    @Transactional(value = "playlistTransactionManager")
    public void purgeOldSoftDeletedData() {
        log.info("[Playlist Purge] Starting scheduled purge for soft-deleted data older than 7 days...");
        Instant threshold = Instant.now().minus(RETENTION_PERIOD);

        try {
            int purgedCurations = curationRepository.purgeDeletedBefore(threshold);
            int purgedPlaylists = playlistRepository.purgeDeletedBefore(threshold);
            int purgedViews = externalContentViewRepository.purgeDeletedBefore(threshold);

            log.info("[Playlist Purge] Completed successfully. Purged curations: {}, playlists: {}, views: {}",
                    purgedCurations, purgedPlaylists, purgedViews);
        } catch (Exception e) {
            log.error("[Playlist Purge] Failed to purge old soft-deleted data: {}", e.getMessage(), e);
        }
    }
}
