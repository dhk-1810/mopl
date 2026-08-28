package org.codeit.sb06.team03.mopl.event;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.entity.ContentReadModel;
import org.codeit.sb06.team03.mopl.service.application.ContentQueryService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurationContentRequestListener {

    private final ContentQueryService contentQueryService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitConfig.CURATION_CONTENT_REQUEST_QUEUE)
    @Transactional(readOnly = true)
    public void handleCurationContentRequest(
            CurationContentRequestEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        log.info("Received CurationContentRequestEvent: {}", event.getContentIds());
        try {
            if (event.getContentIds() == null || event.getContentIds().isEmpty()) {
                channel.basicAck(deliveryTag, false);
                return;
            }

            Set<UUID> uuids = event.getContentIds().stream()
                    .map(idStr -> {
                        try {
                            return UUID.fromString(idStr);
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(HashSet::new, HashSet::add, HashSet::addAll);

            if (uuids.isEmpty()) {
                channel.basicAck(deliveryTag, false);
                return;
            }

            List<ContentReadModel> contents = contentQueryService.getByIds(uuids);
            List<ContentBatchInfoEvent.ContentInfoDto> dtos = contents.stream()
                    .map(c -> new ContentBatchInfoEvent.ContentInfoDto(
                            c.id(),
                            c.type() != null ? c.type().name() : null,
                            c.title(),
                            c.description(),
                            c.thumbnailKey(),
                            c.tags(),
                            c.averageRating(),
                            c.reviewCount(),
                            c.watcherCount()
                    ))
                    .toList();

            if (!dtos.isEmpty()) {
                ContentBatchInfoEvent responseEvent = new ContentBatchInfoEvent(dtos);
                log.info("Publishing ContentBatchInfoEvent to RabbitMQ with {} contents", dtos.size());
                rabbitTemplate.convertAndSend(
                        RabbitConfig.CONTENT_EXCHANGE,
                        RabbitConfig.ROUTING_KEY_CONTENT_BATCH_INFO,
                        responseEvent
                );
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to process CurationContentRequestEvent: {}", e.getMessage(), e);
            channel.basicReject(deliveryTag, false);
        }
    }
}
