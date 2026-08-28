package org.codeit.sb06.team03.mopl.event;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.entity.Content;
import org.codeit.sb06.team03.mopl.repository.ContentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentSagaEventListener {

    private final ContentRepository contentRepository;

    @RabbitListener(queues = RabbitConfig.CONTENT_SAGA_RESPONSE_QUEUE)
    @Transactional("contentTransactionManager")
    public void handleSagaResponse(
            ContentDeletionSagaEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        log.info("Received ContentDeletionSagaEvent response from [{}]: {}", event.participant(), event);

        try {
            Content content = contentRepository.findById(event.contentId()).orElse(null);
            if (content == null) {
                log.warn("Content not found for saga response, contentId: {}", event.contentId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            if ("FAILED".equals(event.status())) {
                log.warn("Saga step failed in participant [{}]. Executing Compensating Transaction for contentId: {}", 
                        event.participant(), event.contentId());
                // Compensating Transaction: 롤백 (소프트 딜리트 취소 및 복구)
                content.restoreActive();
                contentRepository.save(content);
                log.info("Successfully restored Content to ACTIVE status. ContentId: {}", event.contentId());
            } else if ("SUCCESS".equals(event.status())) {
                log.info("Saga step succeeded in participant [{}]. Marking Content as DELETED for contentId: {}", event.participant(), event.contentId());
                content.markAsDeleted();
                contentRepository.save(content);
            }

            // 비즈니스 로직 성공 후 RabbitMQ에 ACK 전송
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Error processing ContentDeletionSagaEvent response: {}", e.getMessage(), e);
            // 재처리 방지 및 DLQ 처리를 위해 requeue=false 로 reject
            channel.basicReject(deliveryTag, false);
        }
    }
}
