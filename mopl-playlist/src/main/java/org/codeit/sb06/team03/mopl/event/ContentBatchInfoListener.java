package org.codeit.sb06.team03.mopl.event;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalContentView;
import org.codeit.sb06.team03.mopl.enums.ContentType;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalContentViewRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentBatchInfoListener {

    private final ExternalContentViewRepository externalContentViewRepository;

    @RabbitListener(queues = RabbitConfig.CONTENT_BATCH_INFO_QUEUE)
    @Transactional(value = "playlistTransactionManager")
    public void handleContentBatchInfo(
            ContentBatchInfoEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        log.info("Received ContentBatchInfoEvent from RabbitMQ: {} items",
                event.getContents() != null ? event.getContents().size() : 0);

        try {
            if (event.getContents() == null || event.getContents().isEmpty()) {
                channel.basicAck(deliveryTag, false);
                return;
            }

            List<ExternalContentView> views = event.getContents().stream()
                    .map(dto -> {
                        ExternalContentView existing = externalContentViewRepository.findById(dto.getContentId()).orElse(null);
                        if (existing != null) {
                            existing.update(
                                    dto.getTitle(),
                                    dto.getDescription(),
                                    dto.getThumbnailKey(),
                                    joinTags(dto.getTags()),
                                    dto.getAverageRating(),
                                    dto.getReviewCount(),
                                    dto.getWatcherCount()
                            );
                            return existing;
                        } else {
                            return ExternalContentView.create(
                                    dto.getContentId(),
                                    ContentType.valueOf(dto.getType()),
                                    dto.getTitle(),
                                    dto.getDescription(),
                                    dto.getThumbnailKey(),
                                    joinTags(dto.getTags()),
                                    dto.getAverageRating(),
                                    dto.getReviewCount(),
                                    dto.getWatcherCount()
                            );
                        }
                    })
                    .toList();

            externalContentViewRepository.saveAll(views);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to process ContentBatchInfoEvent: {}", e.getMessage(), e);
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
