package org.codeit.sb06.team03.mopl.event;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.enums.ContentType;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalContentView;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalContentViewRepository;
import org.codeit.sb06.team03.mopl.service.application.InboxService;
import org.codeit.sb06.team03.mopl.service.application.PlaylistCommandService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentEventListener {

    private final ExternalContentViewRepository externalContentViewRepository;
    private final PlaylistCommandService playlistCommandService;
    private final RabbitTemplate rabbitTemplate;
    private final InboxService inboxService;

    private void sendSagaResponse(ContentDeletionSagaEvent event) {
        org.springframework.amqp.rabbit.connection.CorrelationData correlationData =
                new org.springframework.amqp.rabbit.connection.CorrelationData("playlist-saga-response-" + event.sagaId() + "-" + event.status());
        rabbitTemplate.convertAndSend(
                RabbitConfig.CONTENT_EXCHANGE,
                RabbitConfig.ROUTING_KEY_SAGA_RESPONSE,
                event,
                correlationData
        );
    }

    @RabbitListener(queues = RabbitConfig.CONTENT_UPDATE_QUEUE)
    @Transactional(value = "playlistTransactionManager")
    public void handleContentUpdated(
            ContentEvent.ContentUpdatedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        log.info("Received ContentUpdatedEvent from RabbitMQ: {}", event);
        try {
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
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to update ExternalContentView: {}", e.getMessage(), e);
            channel.basicReject(deliveryTag, false);
        }
    }

    @RabbitListener(queues = RabbitConfig.CONTENT_DELETE_QUEUE)
    @Transactional(value = "playlistTransactionManager")
    public void handleContentDeleted(
            ContentEvent.ContentDeletedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        log.info("Received ContentDeletedEvent from RabbitMQ: {}", event);
        try {
            // 1. Delete curation entries containing this content
            playlistCommandService.deleteCurationByContentId(event.getContentId());
            // 2. Delete local read model
            externalContentViewRepository.deleteById(event.getContentId());
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to delete content in playlist: {}", e.getMessage(), e);
            channel.basicReject(deliveryTag, false);
        }
    }

    @RabbitListener(queues = RabbitConfig.CONTENT_SAGA_START_QUEUE)
    @Transactional(value = "playlistTransactionManager")
    public void handleContentDeletionSaga(
            ContentDeletionSagaEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        log.info("Received ContentDeletionSagaEvent START in mopl-playlist: {}", event);

        String messageId = "saga-start-" + event.sagaId() + "-playlist";
        if (inboxService.isAlreadyProcessed(messageId)) {
            log.info("[Inbox] Saga start already processed in playlist. Skipping duplicate message: {}", messageId);
            // 멱등성 보장을 위해 이전 성공 응답 재전송 후 ACK
            sendSagaResponse(ContentDeletionSagaEvent.success(event.sagaId(), event.contentId(), "PLAYLIST"));
            channel.basicAck(deliveryTag, false);
            return;
        }

        try {
            // 1. 플레이리스트 연관 항목 및 CQRS View 뷰 삭제
            playlistCommandService.deleteCurationByContentId(event.contentId());
            externalContentViewRepository.deleteById(event.contentId());

            // 2. Inbox 테이블에 처리 완료 기록
            inboxService.recordProcessed(messageId, "CONTENT_SAGA", "ContentDeletionSagaEvent", event.toString());

            // 3. 성공 이벤트 응답 (mopl-content에 전달)
            sendSagaResponse(ContentDeletionSagaEvent.success(event.sagaId(), event.contentId(), "PLAYLIST"));

            // 4. 비즈니스 로직 및 응답 발행 성공 후 RabbitMQ에 ACK 전송
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to delete playlist curation for contentId: {}", event.contentId(), e);
            // 실패 응답 전송
            sendSagaResponse(ContentDeletionSagaEvent.failed(event.sagaId(), event.contentId(), "PLAYLIST", e.getMessage()));
            // 비즈니스 실패 처리가 완료되었으므로 메시지 버림(requeue=false)
            channel.basicReject(deliveryTag, false);
        }
    }

    private String joinTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(",", tags);
    }
}
