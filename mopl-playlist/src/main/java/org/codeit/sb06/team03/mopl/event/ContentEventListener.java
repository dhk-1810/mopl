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
import org.springframework.amqp.support.AmqpHeaders;
import org.codeit.sb06.team03.mopl.dto.PlaylistCurationBackupDto;
import org.codeit.sb06.team03.mopl.entity.Curation;
import org.codeit.sb06.team03.mopl.repository.CurationRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentEventListener {

    private final ExternalContentViewRepository externalContentViewRepository;
    private final CurationRepository curationRepository;
    private final PlaylistCommandService playlistCommandService;
    private final InboxService inboxService;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private void sendSagaResponse(ContentDeletionSagaEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(RabbitConfig.ROUTING_KEY_SAGA_RESPONSE, event.sagaId().toString(), payload);
            log.info("[Kafka] Sent saga response from playlist: {}", payload);
        } catch (Exception e) {
            log.error("[Kafka] Failed to send saga response from playlist for event: {}", event, e);
        }
    }

    @RabbitListener(queues = RabbitConfig.CONTENT_UPDATE_QUEUE)
    @Transactional(value = "playlistTransactionManager")
    public void handleContentUpdated(
            ContentUpdatedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        log.info("Received ContentUpdatedEvent from RabbitMQ: {}", event);
        try {
            ExternalContentView contentView = externalContentViewRepository.findById(event.contentId())
                    .orElseGet(() -> ExternalContentView.create(
                            event.contentId(),
                            ContentType.valueOf(event.type()),
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
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to update ExternalContentView: {}", e.getMessage(), e);
            channel.basicReject(deliveryTag, false);
        }
    }

    @RabbitListener(queues = RabbitConfig.CONTENT_SAGA_START_QUEUE)
    @Transactional(value = "playlistTransactionManager")
    public void handleContentDeletionSagaRabbit(
            ContentDeletionSagaEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        log.info("Received ContentDeletionSagaEvent START from RabbitMQ in mopl-playlist: {}", event);
        try {
            processContentDeletionSaga(event);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Error in rabbit saga listener: {}", e.getMessage(), e);
            channel.basicReject(deliveryTag, false);
        }
    }

    @KafkaListener(topics = RabbitConfig.ROUTING_KEY_SAGA_START, groupId = "${spring.kafka.consumer.group-id:playlist-service-group}", autoStartup = "#{ '${mopl.saga.type:grpc}' == 'cdc-kafka' }")
    @Transactional(value = "playlistTransactionManager")
    public void handleContentDeletionSagaKafka(String payload) {
        log.info("Received ContentDeletionSagaEvent START from Kafka in mopl-playlist: {}", payload);

        ContentDeletionSagaEvent event;
        try {
            event = objectMapper.readValue(payload, ContentDeletionSagaEvent.class);
        } catch (Exception e) {
            log.error("Failed to deserialize ContentDeletionSagaEvent from Kafka payload: {}", payload, e);
            return;
        }

        processContentDeletionSaga(event);
    }

    private void processContentDeletionSaga(ContentDeletionSagaEvent event) {
        if ("COMPENSATE".equalsIgnoreCase(event.action())) {
            handleContentCompensate(event);
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
            // 1. 삭제 전 Redis 백업 (Curation 목록 및 CQRS View)
            backupCurationsAndContentView(event);

            // 2. 플레이리스트 연관 항목 및 CQRS View 삭제
            playlistCommandService.deleteCurationByContentId(event.contentId());
            externalContentViewRepository.deleteById(event.contentId());

            // 3. Inbox 테이블에 처리 완료 기록
            inboxService.recordProcessed(messageId, "CONTENT_SAGA", "ContentDeletionSagaEvent", event.toString());

            // 4. 성공 이벤트 응답 (Kafka 전송)
            sendSagaResponse(ContentDeletionSagaEvent.success(event.sagaId(), event.contentId(), "PLAYLIST"));
        } catch (Exception e) {
            log.error("Failed to delete playlist curation for contentId: {}", event.contentId(), e);
            // 실패 응답 전송 (Kafka)
            sendSagaResponse(ContentDeletionSagaEvent.failed(event.sagaId(), event.contentId(), "PLAYLIST", e.getMessage()));
            throw new RuntimeException("Error processing ContentDeletionSagaEvent in playlist", e);
        }
    }

    private void backupCurationsAndContentView(ContentDeletionSagaEvent event) {
        try {
            List<Curation> curations = curationRepository.findAllByContentId(event.contentId());
            ExternalContentView view = externalContentViewRepository.findById(event.contentId()).orElse(null);

            if (!curations.isEmpty() || view != null) {
                List<PlaylistCurationBackupDto.CurationItem> curationItems = curations.stream()
                        .map(c -> new PlaylistCurationBackupDto.CurationItem(
                                c.getId().getPlaylistId(),
                                c.getId().getContentId(),
                                c.getContentTitle()))
                        .toList();

                PlaylistCurationBackupDto.ExternalContentViewItem viewItem = view != null ?
                        new PlaylistCurationBackupDto.ExternalContentViewItem(
                                view.getId(),
                                view.getType(),
                                view.getTitle(),
                                view.getDescription(),
                                view.getThumbnailKey(),
                                view.getTags(),
                                view.getAverageRating(),
                                view.getReviewCount(),
                                view.getWatcherCount()
                        ) : null;

                PlaylistCurationBackupDto backupDto = new PlaylistCurationBackupDto(curationItems, viewItem);
                String backupJson = objectMapper.writeValueAsString(backupDto);
                String backupKey = "backup:playlist:curation:" + event.sagaId();
                redisTemplate.opsForValue().set(backupKey, backupJson, Duration.ofMinutes(10));
                log.info("Successfully backed up {} curation(s) and view for sagaId: {}", curationItems.size(), event.sagaId());
            }
        } catch (Exception e) {
            log.error("Failed to backup curations to Redis for sagaId: {}", event.sagaId(), e);
        }
    }

    private void handleContentCompensate(ContentDeletionSagaEvent event) {
        String messageId = "saga-compensate-" + event.sagaId() + "-playlist";
        if (inboxService.isAlreadyProcessed(messageId)) {
            log.info("[Inbox] Saga compensate already processed in playlist. Skipping duplicate: {}", messageId);
            return;
        }

        String backupKey = "backup:playlist:curation:" + event.sagaId();
        String backupJson = redisTemplate.opsForValue().get(backupKey);
        if (backupJson == null) {
            log.warn("No backup found in Redis for saga compensation, sagaId: {}. Skipping restore.", event.sagaId());
            inboxService.recordProcessed(messageId, "CONTENT_SAGA_COMPENSATE", "ContentDeletionSagaEvent", event.toString());
            return;
        }

        try {
            PlaylistCurationBackupDto backupDto = objectMapper.readValue(backupJson, PlaylistCurationBackupDto.class);
            if (backupDto.curations() != null && !backupDto.curations().isEmpty()) {
                List<Curation> toRestore = backupDto.curations().stream()
                        .map(item -> Curation.create(item.playlistId(), item.contentId(), item.contentTitle()))
                        .toList();
                playlistCommandService.restoreCurations(toRestore);
                log.info("Restored {} curation(s) for contentId: {}", toRestore.size(), event.contentId());
            }

            if (backupDto.contentView() != null) {
                PlaylistCurationBackupDto.ExternalContentViewItem v = backupDto.contentView();
                ExternalContentView restoredView = ExternalContentView.create(
                        v.id(), v.type(), v.title(), v.description(), v.thumbnailKey(),
                        v.tags(), v.averageRating(), v.reviewCount(), v.watcherCount()
                );
                externalContentViewRepository.save(restoredView);
                log.info("Restored ExternalContentView for contentId: {}", event.contentId());
            }

            redisTemplate.delete(backupKey);
            inboxService.recordProcessed(messageId, "CONTENT_SAGA_COMPENSATE", "ContentDeletionSagaEvent", event.toString());
            log.info("Successfully completed saga compensation in playlist for sagaId: {}", event.sagaId());
        } catch (Exception e) {
            log.error("Failed to restore playlist curations for sagaId: {}", event.sagaId(), e);
            throw new RuntimeException("Error compensating playlist curations", e);
        }
    }

    private String joinTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(",", tags);
    }
}
