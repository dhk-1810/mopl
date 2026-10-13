package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.service.PlaylistQueryService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class PlaylistInternalEventListener {

    private final PlaylistQueryService playlistQueryService;
    private final RabbitTemplate rabbitTemplate;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubscriptionCreatedEvent(SubscriptionCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.PLAYLIST_EXCHANGE,
                RabbitConfig.PLAYLIST_SUBSCRIBED_ROUTING_KEY,
                event
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCurationAddedEvent(CurationAddedEvent event) {
        List<UUID> subscriberIds = playlistQueryService.getSubscriberIds(event.playlistId());

        rabbitTemplate.convertAndSend(
                RabbitConfig.PLAYLIST_EXCHANGE,
                RabbitConfig.CURATION_ADDED_ROUTING_KEY,
                new CurationAddedEvent(
                        event.playlistId(),
                        event.playlistTitle(),
                        event.contentTitle(),
                        subscriberIds
                )
        );
    }
}
