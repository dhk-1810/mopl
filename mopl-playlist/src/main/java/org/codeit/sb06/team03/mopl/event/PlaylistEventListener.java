package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.follow.domain.Followee;
import org.codeit.sb06.team03.mopl.follow.exception.FolloweeNotFoundException;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.follow.service.FolloweeQueryService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class PlaylistEventListener {

    private final FolloweeQueryService followeeQueryService;
    private final PlaylistQueryService playlistQueryService;
    private final PlaylistCommandService playlistCommandService;
    private final RabbitTemplate rabbitTemplate;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePlaylistCreatedEvent(PlaylistEvent.PlaylistCreatedEvent event) {
        Followee followee = followeeQueryService.findById(event.getOwnerId())
                .orElseThrow(() -> new FolloweeNotFoundException(event.getOwnerId()));
        List<UUID> followerIds = followee.getFollowers().stream()
                .map(follower -> follower.getId().getFollowerId())
                .toList();

        rabbitTemplate.convertAndSend(
                RabbitConfig.PLAYLIST_EXCHANGE,
                RabbitConfig.ROUTING_KEY_PLAYLIST_CREATED,
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
        rabbitTemplate.convertAndSend(
                RabbitConfig.PLAYLIST_EXCHANGE,
                "playlist.subscribed",
                event
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCurationAddedEvent(PlaylistEvent.CurationAddedEvent event) {
        List<UUID> subscriberIds = playlistQueryService.getSubscriberIds(event.getPlaylistId());

        rabbitTemplate.convertAndSend(
                RabbitConfig.PLAYLIST_EXCHANGE,
                RabbitConfig.ROUTING_KEY_CURATION_ADDED,
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
        playlistCommandService.deleteCascadeByPlaylistId(event.getPlaylistId());
    }
}
