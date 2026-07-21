package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.service.application.WatchingSessionCommandService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WatchingSessionEventListener {

    private final WatchingSessionCommandService watchingSessionCommandService;

    @RabbitListener(queues = RabbitConfig.WS_CREATE_QUEUE)
    public void handleWatchingSessionCreate(WatchingSessionCreateRequestEvent event) {
        log.info("Received WatchingSessionCreateRequestEvent from RabbitMQ: {}", event);
        try {
            watchingSessionCommandService.createWithId(
                    event.sessionId(),
                    event.contentId(),
                    event.watcherId(),
                    event.createdAt()
            );
            log.info("Successfully created watching session from queue for watcher: {}", event.watcherId());
        } catch (Exception e) {
            log.error("Failed to create watching session asynchronously from event: {}", event, e);
        }
    }

    @RabbitListener(queues = RabbitConfig.WS_DELETE_QUEUE)
    public void handleWatchingSessionDelete(WatchingSessionDeleteRequestEvent event) {
        log.info("Received WatchingSessionDeleteRequestEvent from RabbitMQ: {}", event);
        try {
            if (event.sessionId() != null) {
                watchingSessionCommandService.delete(event.sessionId());
            } else if (event.watcherId() != null) {
                watchingSessionCommandService.deleteByWatcherId(event.watcherId());
            }
            log.info("Successfully deleted watching session from queue for event: {}", event);
        } catch (Exception e) {
            log.error("Failed to delete watching session asynchronously from event: {}", event, e);
        }
    }
}
