package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.service.application.WatchingSessionCommandService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WatchingSessionEventListener {

    private final WatchingSessionCommandService watchingSessionCommandService;

    @EventListener
    public void handleWatchingSessionCreate(WatchingSessionCreateRequestEvent event) {
        log.info("Received WatchingSessionCreateRequestEvent: {}", event);
        try {
            watchingSessionCommandService.createWithId(
                    event.sessionId(),
                    event.contentId(),
                    event.watcherId(),
                    event.createdAt()
            );
            log.info("Successfully created watching session for watcher: {}", event.watcherId());
        } catch (Exception e) {
            log.error("Failed to create watching session from event: {}", event, e);
        }
    }

    @EventListener
    public void handleWatchingSessionDelete(WatchingSessionDeleteRequestEvent event) {
        log.info("Received WatchingSessionDeleteRequestEvent: {}", event);
        try {
            if (event.sessionId() != null) {
                watchingSessionCommandService.delete(event.sessionId());
            } else if (event.watcherId() != null) {
                watchingSessionCommandService.deleteByWatcherId(event.watcherId());
            }
            log.info("Successfully deleted watching session for event: {}", event);
        } catch (Exception e) {
            log.error("Failed to delete watching session from event: {}", event, e);
        }
    }
}
