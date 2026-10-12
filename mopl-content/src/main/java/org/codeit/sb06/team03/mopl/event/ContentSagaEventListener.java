package org.codeit.sb06.team03.mopl.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.entity.Content;
import org.codeit.sb06.team03.mopl.entity.SagaInstance;
import org.codeit.sb06.team03.mopl.repository.ContentRepository;
import org.codeit.sb06.team03.mopl.repository.SagaInstanceRepository;
import org.codeit.sb06.team03.mopl.service.application.InboxService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnExpression("'${mopl.saga.type:grpc}' == 'outbox-polling' || '${mopl.saga.type:grpc}' == 'cdc-kafka'")
@RequiredArgsConstructor
public class ContentSagaEventListener {

    private final ContentRepository contentRepository;
    private final SagaInstanceRepository sagaInstanceRepository;
    private final InboxService inboxService;
    private final RabbitTemplate rabbitTemplate;
    private final ContentEventPublisher contentEventPublisher;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = RabbitConfig.ROUTING_KEY_SAGA_RESPONSE, groupId = "${spring.kafka.consumer.group-id:content-service-group}")
    @Transactional("contentTransactionManager")
    public void handleSagaResponseKafka(String payload) {
        log.info("Received ContentDeletionSagaEvent response from Kafka in mopl-content: {}", payload);
        try {
            ContentDeletionSagaEvent event = objectMapper.readValue(payload, ContentDeletionSagaEvent.class);
            processSagaResponse(event);
        } catch (Exception e) {
            log.error("Failed to deserialize saga response from Kafka: {}", payload, e);
        }
    }

    @RabbitListener(queues = RabbitConfig.CONTENT_SAGA_RESPONSE_QUEUE)
    @Transactional("contentTransactionManager")
    public void handleSagaResponse(
            ContentDeletionSagaEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        log.info("Received ContentDeletionSagaEvent response from RabbitMQ [{}]: {}", event.participant(), event);
        try {
            processSagaResponse(event);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Error processing ContentDeletionSagaEvent response from RabbitMQ: {}", e.getMessage(), e);
            channel.basicReject(deliveryTag, false);
        }
    }

    private void processSagaResponse(ContentDeletionSagaEvent event) {
        String messageId = "saga-resp-" + event.sagaId() + "-" + event.participant() + "-" + event.status();
        if (inboxService.isAlreadyProcessed(messageId)) {
            log.info("[Inbox] Saga response already processed. Skipping duplicate message: {}", messageId);
            return;
        }

        try {
            SagaInstance sagaInstance = sagaInstanceRepository.findById(event.sagaId()).orElse(null);
            if (sagaInstance == null) {
                log.warn("SagaInstance not found for saga response, sagaId: {}", event.sagaId());
                inboxService.recordProcessed(messageId, "CONTENT_SAGA", "ContentDeletionSagaEvent", event.toString());
                return;
            }

            // [문제 3] 지연 메시지 방어: 이미 완료, 실패, 타임아웃된 Saga는 무시
            if (sagaInstance.isFinished()) {
                log.warn("Saga [{}] is already finished with status [{}]. Discarding late arrival response from [{}].",
                        event.sagaId(), sagaInstance.getStatus(), event.participant());
                inboxService.recordProcessed(messageId, "CONTENT_SAGA", "ContentDeletionSagaEvent", event.toString());
                return;
            }

            // 참여자 응답 상태 기록
            sagaInstance.updateParticipant(event.participant(), event.status());

            Content content = contentRepository.findById(event.contentId()).orElse(null);

            // [문제 1] 한 곳이라도 실패한 경우 -> 즉시 롤백 및 보상 트랜잭션 전파
            if ("FAILED".equalsIgnoreCase(event.status()) || sagaInstance.isAnyFailed()) {
                log.warn("Saga step failed in participant [{}]. Executing Compensating Transaction for contentId: {}",
                        event.participant(), event.contentId());
                sagaInstance.markFailed();
                sagaInstanceRepository.save(sagaInstance);

                if (content != null) {
                    content.restoreActive();
                    contentRepository.save(content);
                    log.info("Successfully restored Content to ACTIVE status. ContentId: {}", event.contentId());
                }

                // 타 서비스(Playlist 등)에 보상 트랜잭션 이벤트(COMPENSATE) 전파
                publishCompensation(event.sagaId(), event.contentId());
            } else if (sagaInstance.isAllSuccess()) {
                // [문제 1] 두 참여자 모두 SUCCESS인 경우에만 최종 DELETED 전이
                log.info("All participants succeeded for saga [{}]. Marking Content as DELETED for contentId: {}",
                        event.sagaId(), event.contentId());
                sagaInstance.markCompleted();
                sagaInstanceRepository.save(sagaInstance);

                if (content != null) {
                    content.markAsDeleted();
                    contentRepository.save(content);
                }
            } else {
                // 아직 다른 참여자 응답 대기 중
                log.info("Participant [{}] succeeded for saga [{}]. Waiting for remaining participants...",
                        event.participant(), event.sagaId());
                sagaInstanceRepository.save(sagaInstance);
            }

            // Inbox 처리 기록
            inboxService.recordProcessed(messageId, "CONTENT_SAGA", "ContentDeletionSagaEvent", event.toString());
        } catch (Exception e) {
            log.error("Error handling saga response logic: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void publishCompensation(UUID sagaId, UUID contentId) {
        try {
            ContentDeletionSagaEvent compensateEvent = ContentDeletionSagaEvent.compensate(sagaId, contentId);

            // 1. Kafka로 보상 이벤트 전송
            String payload = objectMapper.writeValueAsString(compensateEvent);
            kafkaTemplate.send(RabbitConfig.ROUTING_KEY_SAGA_START, sagaId.toString(), payload);
            log.info("[Kafka] Sent COMPENSATE event for sagaId: {}, contentId: {}", sagaId, contentId);

            // 2. RabbitMQ로 보상 이벤트 직접 발행
            CorrelationData correlationData = new CorrelationData("saga-compensate-" + sagaId);
            rabbitTemplate.convertAndSend(
                    RabbitConfig.CONTENT_EXCHANGE,
                    RabbitConfig.ROUTING_KEY_SAGA_START,
                    compensateEvent,
                    correlationData
            );

            // 3. Outbox/CDC를 위해 Outbox 테이블에도 저장
            contentEventPublisher.publishContentDeletionSagaCompensate(compensateEvent);

            log.info("Successfully initiated COMPENSATE propagation for sagaId: {}, contentId: {}", sagaId, contentId);
        } catch (Exception e) {
            log.error("Failed to publish COMPENSATE event for sagaId: {}, contentId: {}", sagaId, contentId, e);
        }
    }
}
