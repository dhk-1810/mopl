package org.codeit.sb06.team03.mopl.event;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.kafka.annotation.KafkaListener;
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
    private final ObjectMapper objectMapper;

    private void sendSagaResponse(ContentDeletionSagaEvent event) {
        CorrelationData correlationData =
                new CorrelationData("playlist-saga-response-" + event.sagaId() + "-" + event.status());
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

    @KafkaListener(topics = RabbitConfig.ROUTING_KEY_SAGA_START, groupId = "playlist-service-group")
    @Transactional(value = "playlistTransactionManager")
    public void handleContentDeletionSaga(String payload) {
        log.info("Received ContentDeletionSagaEvent START from Kafka in mopl-playlist: {}", payload);

        ContentDeletionSagaEvent event;
        try {
            event = objectMapper.readValue(payload, ContentDeletionSagaEvent.class);
        } catch (Exception e) {
            log.error("Failed to deserialize ContentDeletionSagaEvent from Kafka payload: {}", payload, e);
            return;
        }

        String messageId = "saga-start-" + event.sagaId() + "-playlist";
        if (inboxService.isAlreadyProcessed(messageId)) {
            log.info("[Inbox] Saga start already processed in playlist. Skipping duplicate message: {}", messageId);
            // 멱등성 보장을 위해 이전 성공 응답 재전송
            sendSagaResponse(ContentDeletionSagaEvent.success(event.sagaId(), event.contentId(), "PLAYLIST"));
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
        } catch (Exception e) {
            log.error("Failed to delete playlist curation for contentId: {}", event.contentId(), e);
            // 실패 응답 전송
            sendSagaResponse(ContentDeletionSagaEvent.failed(event.sagaId(), event.contentId(), "PLAYLIST", e.getMessage()));
            throw new RuntimeException("Error processing ContentDeletionSagaEvent in playlist", e);
        }
    }

    private String joinTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(",", tags);
    }
}
