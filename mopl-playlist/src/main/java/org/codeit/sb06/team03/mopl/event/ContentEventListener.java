package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.playlist.config.application.PlaylistCommandService;
import org.codeit.sb06.team03.mopl.domain.entity.cqrs.ExternalContentView;
import org.codeit.sb06.team03.mopl.playlist.config.infra.out.cqrs.ExternalContentViewRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentEventListener {

    private final ExternalContentViewRepository externalContentViewRepository;
    private final PlaylistCommandService playlistCommandService;

    @RabbitListener(queues = RabbitConfig.CONTENT_CREATE_QUEUE)
    @Transactional(value = "playlistTransactionManager")
    public void handleContentCreated(ContentEvent.ContentCreatedEvent event) {
        log.info("Received ContentCreatedEvent from RabbitMQ: {}", event);
        ExternalContentView contentView = ExternalContentView.create(
                event.contentId(),
                event.type(),
                event.title(),
                event.description(),
                event.thumbnailKey(),
                joinTags(event.tags()),
                event.averageRating(),
                event.reviewCount(),
                event.watcherCount()
        );
        externalContentViewRepository.save(contentView);
    }

    @RabbitListener(queues = RabbitConfig.CONTENT_UPDATE_QUEUE)
    @Transactional(value = "playlistTransactionManager")
    public void handleContentUpdated(ContentEvent.ContentUpdatedEvent event) {
        log.info("Received ContentUpdatedEvent from RabbitMQ: {}", event);
        ExternalContentView contentView = externalContentViewRepository.findById(event.contentId())
                .orElseGet(() -> ExternalContentView.create(
                        event.contentId(),
                        event.type(),
                        event.title(),
                        event.description(),
                        event.thumbnailKey(),
                        joinTags(event.tags()),
                        event.averageRating(),
                        event.reviewCount(),
                        event.watcherCount()
                ));
        contentView.update(
                event.title(),
                event.description(),
                event.thumbnailKey(),
                joinTags(event.tags()),
                event.averageRating(),
                event.reviewCount(),
                event.watcherCount()
        );
        externalContentViewRepository.save(contentView);
    }

    @RabbitListener(queues = RabbitConfig.CONTENT_DELETE_QUEUE)
    @Transactional(value = "playlistTransactionManager")
    public void handleContentDeleted(ContentEvent.ContentDeletedEvent event) {
        log.info("Received ContentDeletedEvent from RabbitMQ: {}", event);
        // 1. Delete curation entries containing this content
        playlistCommandService.deleteCurationByContentId(event.contentId());
        // 2. Delete local read model
        externalContentViewRepository.deleteById(event.contentId());
    }

    private String joinTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(",", tags);
    }
}
