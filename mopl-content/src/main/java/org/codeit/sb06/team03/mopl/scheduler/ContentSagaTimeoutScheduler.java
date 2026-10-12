package org.codeit.sb06.team03.mopl.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.entity.Content;
import org.codeit.sb06.team03.mopl.entity.SagaInstance;
import org.codeit.sb06.team03.mopl.enums.ContentStatus;
import org.codeit.sb06.team03.mopl.enums.SagaStatus;
import org.codeit.sb06.team03.mopl.event.ContentDeletionSagaEvent;
import org.codeit.sb06.team03.mopl.event.ContentEventPublisher;
import org.codeit.sb06.team03.mopl.repository.ContentRepository;
import org.codeit.sb06.team03.mopl.repository.SagaInstanceRepository;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnExpression("'${mopl.saga.type:grpc}' == 'outbox-polling' || '${mopl.saga.type:grpc}' == 'cdc-kafka'")
@RequiredArgsConstructor
public class ContentSagaTimeoutScheduler {

    private final ContentRepository contentRepository;
    private final SagaInstanceRepository sagaInstanceRepository;
    private final ContentEventPublisher contentEventPublisher;
    private final RabbitTemplate rabbitTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // Saga 대기 타임아웃 임계치 (3분)
    private static final Duration TIMEOUT_DURATION = Duration.ofMinutes(3);

    @Scheduled(fixedDelay = 60000) // 1분 주기 실행
    @Transactional("contentTransactionManager")
    public void handleTimedOutSaga() {
        Instant threshold = Instant.now().minus(TIMEOUT_DURATION);

        // 1. SagaInstance 기반 타임아웃 감지 및 전이
        List<SagaInstance> timedOutSagas = sagaInstanceRepository.findAllByStatusAndCreatedAtBefore(SagaStatus.STARTED, threshold);
        if (!timedOutSagas.isEmpty()) {
            log.warn("Found {} timed out Saga instances. Executing automatic compensation rollback.", timedOutSagas.size());
            for (SagaInstance saga : timedOutSagas) {
                log.warn("Saga timeout reached for sagaId: {}, contentId: {}. Marking TIMED_OUT.", saga.getId(), saga.getContentId());
                saga.markTimedOut();
                sagaInstanceRepository.save(saga);

                contentRepository.findById(saga.getContentId()).ifPresent(content -> {
                    content.restoreActive();
                    contentRepository.save(content);
                    log.info("Restored Content status to ACTIVE for contentId: {}", content.getId());
                });

                // 혹시 이미 성공했던 참여자가 있을 수 있으므로 보상 트랜잭션 전파
                publishCompensation(saga.getId(), saga.getContentId());
            }
        }

        // 2. 레거시/안전망: Content가 여전히 DELETING인 건 감지 및 복구
        List<Content> timedOutContents = contentRepository.findAllByStatusAndUpdatedAtBefore(ContentStatus.DELETING, threshold);
        if (!timedOutContents.isEmpty()) {
            log.warn("Found {} timed out deleting contents in fallback check. Restoring status to ACTIVE.", timedOutContents.size());
            for (Content content : timedOutContents) {
                content.restoreActive();
                contentRepository.save(content);
            }
        }
    }

    private void publishCompensation(UUID sagaId, UUID contentId) {
        try {
            ContentDeletionSagaEvent compensateEvent = ContentDeletionSagaEvent.compensate(sagaId, contentId);

            // Kafka 보상 이벤트 전송
            String payload = objectMapper.writeValueAsString(compensateEvent);
            kafkaTemplate.send(RabbitConfig.ROUTING_KEY_SAGA_START, sagaId.toString(), payload);

            // RabbitMQ 보상 이벤트 발행
            CorrelationData correlationData = new CorrelationData("saga-timeout-compensate-" + sagaId);
            rabbitTemplate.convertAndSend(
                    RabbitConfig.CONTENT_EXCHANGE,
                    RabbitConfig.ROUTING_KEY_SAGA_START,
                    compensateEvent,
                    correlationData
            );

            // Outbox/CDC를 위해 Outbox 테이블에도 저장
            contentEventPublisher.publishContentDeletionSagaCompensate(compensateEvent);

            log.info("Successfully sent timeout COMPENSATE propagation for sagaId: {}, contentId: {}", sagaId, contentId);
        } catch (Exception e) {
            log.error("Failed to publish timeout COMPENSATE event for sagaId: {}, contentId: {}", sagaId, contentId, e);
        }
    }
}
