package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.service.PlaylistQueryService;
import org.codeit.sb06.team03.mopl.service.application.PlaylistCommandService;
import org.codeit.sb06.team03.mopl.service.FolloweeQueryService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class PlaylistEventListener {

    private final PlaylistQueryService playlistQueryService;
    private final PlaylistCommandService playlistCommandService;
    private final FolloweeQueryService followeeQueryService;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePlaylistCreatedEvent(PlaylistEvent.PlaylistCreatedEvent event) {
        Set<UUID> followerIds = followeeQueryService.getFollowerIds(event.getOwnerId());

        eventPublisher.publishEvent(
                new PlaylistEvent.PlaylistCreatedEvent(
                        event.getOwnerId(),
                        event.getOwnerName(),
                        event.getPlaylistId(),
                        event.getPlaylistTitle(),
                        followerIds
                )
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubscriptionCreatedEvent(PlaylistEvent.SubscriptionCreatedEvent event) {
        eventPublisher.publishEvent(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCurationAddedEvent(PlaylistEvent.CurationAddedEvent event) {
        List<UUID> subscriberIds = playlistQueryService.getSubscriberIds(event.getPlaylistId());

        eventPublisher.publishEvent(
                new PlaylistEvent.CurationAddedEvent(
                        event.getPlaylistId(),
                        event.getPlaylistTitle(),
                        event.getContentTitle(),
                        subscriberIds
                )
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePlaylistDeletedEvent(PlaylistEvent.PlaylistDeletedEvent event) {
        playlistCommandService.deleteCurationByContentId(event.getPlaylistId());
    }
}
