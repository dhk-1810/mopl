package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.enums.ContentType;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalContentView;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalContentViewRepository;
import org.codeit.sb06.team03.mopl.service.application.PlaylistCommandService;
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
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    private void sendSagaResponse(ContentDeletionSagaEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.CONTENT_EXCHANGE,
                RabbitConfig.ROUTING_KEY_SAGA_RESPONSE,
                event
        );
    }



    @RabbitListener(queues = RabbitConfig.CONTENT_UPDATE_QUEUE)
    @Transactional(value = "playlistTransactionManager")
    public void handleContentUpdated(ContentEvent.ContentUpdatedEvent event) {
        log.info("Received ContentUpdatedEvent from RabbitMQ: {}", event);
        ExternalContentView contentView = externalContentViewRepository.findById(event.getContentId())
                .orElseGet(() -> ExternalContentView.create(
                        event.getContentId(),
                        ContentType.valueOf(event.getType()),
                        event.getTitle(),
                        event.getDescription(),
                        event.getThumbnailKey(),
                        joinTags(event.getTags()),
                        event.getAverageRating(),
                        event.getReviewCount(),
                        event.getWatcherCount()
                ));
        contentView.update(
                event.getTitle(),
                event.getDescription(),
                event.getThumbnailKey(),
                joinTags(event.getTags()),
                event.getAverageRating(),
                event.getReviewCount(),
                event.getWatcherCount()
        );
        externalContentViewRepository.save(contentView);
    }

    @RabbitListener(queues = RabbitConfig.CONTENT_DELETE_QUEUE)
    @Transactional(value = "playlistTransactionManager")
    public void handleContentDeleted(ContentEvent.ContentDeletedEvent event) {
        log.info("Received ContentDeletedEvent from RabbitMQ: {}", event);
        // 1. Delete curation entries containing this content
        playlistCommandService.deleteCurationByContentId(event.getContentId());
        // 2. Delete local read model
        externalContentViewRepository.deleteById(event.getContentId());
    }

    @RabbitListener(queues = RabbitConfig.CONTENT_SAGA_START_QUEUE)
    @Transactional(value = "playlistTransactionManager")
    public void handleContentDeletionSaga(ContentDeletionSagaEvent event) {
        log.info("Received ContentDeletionSagaEvent START in mopl-playlist: {}", event);
        try {
            // 1. 플레이리스트 연관 항목 및 CQRS View 뷰 삭제
            playlistCommandService.deleteCurationByContentId(event.contentId());
            externalContentViewRepository.deleteById(event.contentId());

            // 2. 성공 이벤트 응답 (mopl-content에 전달)
            sendSagaResponse(ContentDeletionSagaEvent.success(event.sagaId(), event.contentId(), "PLAYLIST"));
        } catch (Exception e) {
            log.error("Failed to delete playlist curation for contentId: {}", event.contentId(), e);
            sendSagaResponse(ContentDeletionSagaEvent.failed(event.sagaId(), event.contentId(), "PLAYLIST", e.getMessage()));
        }
    }

    private String joinTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(",", tags);
    }
}
