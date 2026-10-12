package org.codeit.sb06.team03.mopl.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.entity.Content;
import org.codeit.sb06.team03.mopl.repository.ContentRepository;
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
            Content content = contentRepository.findById(event.contentId()).orElse(null);
            if (content == null) {
                log.warn("Content not found for saga response, contentId: {}", event.contentId());
                inboxService.recordProcessed(messageId, "CONTENT_SAGA", "ContentDeletionSagaEvent", event.toString());
                return;
            }

            if ("FAILED".equals(event.status())) {
                log.warn("Saga step failed in participant [{}]. Executing Compensating Transaction for contentId: {}",
                        event.participant(), event.contentId());
                // Compensating Transaction: 롤백 (소프트 딜리트 취소 및 복구)
                content.restoreActive();
                contentRepository.save(content);
                log.info("Successfully restored Content to ACTIVE status. ContentId: {}", event.contentId());

                // 타 서비스(Playlist 등)에 보상 트랜잭션 이벤트(COMPENSATE) 전파
                publishCompensation(event.sagaId(), event.contentId());
            } else if ("SUCCESS".equals(event.status())) {
                log.info("Saga step succeeded in participant [{}]. Marking Content as DELETED for contentId: {}",
                        event.participant(), event.contentId());
                content.markAsDeleted();
                contentRepository.save(content);
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
