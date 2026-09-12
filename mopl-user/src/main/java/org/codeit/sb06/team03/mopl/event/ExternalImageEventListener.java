package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalImageView;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalImageViewRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalImageEventListener {

    private final ExternalImageViewRepository externalImageViewRepository;

    @RabbitListener(queues = RabbitConfig.IMAGE_PRESIGNED_URL_CREATED_QUEUE)
    @Transactional(value = "userTransactionManager")
    public void handleImagePresignedUrlCreated(ImagePresignedUrlCreatedEvent event) {
        log.info("Received ImagePresignedUrlCreatedEvent via RabbitMQ. key: {}", event.key());
        try {
            ExternalImageView imageView = externalImageViewRepository.findByImageKey(event.key())
                    .orElseGet(() -> ExternalImageView.create(event.key(), event.presignedUrl(), event.exp()));

            imageView.update(event.presignedUrl(), event.exp());
            externalImageViewRepository.save(imageView);
            log.info("Successfully updated ExternalImageView for key: {}", event.key());
        } catch (Exception e) {
            log.error("Failed to handle ImagePresignedUrlCreatedEvent for key: {}", event.key(), e);
            throw new RuntimeException("Failed to update ExternalImageView", e);
        }
    }
}
