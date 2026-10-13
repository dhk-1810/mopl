package org.codeit.sb06.team03.mopl.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.KafkaConfig;
import org.codeit.sb06.team03.mopl.entity.ExternalImageView;
import org.codeit.sb06.team03.mopl.repository.ExternalImageViewRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalImageEventListener {

    private final ExternalImageViewRepository externalImageViewRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaConfig.TOPIC_IMAGE_PRESIGNED_URL_EVENTS, groupId = "content-image-group")
    @Transactional(value = "contentTransactionManager")
    public void handleImagePresignedUrlCreated(String payload) {
        log.info("Received ImagePresignedUrlKafkaEvent via Kafka in mopl-content: {}", payload);
        try {
            ImagePresignedUrlKafkaEvent event = objectMapper.readValue(payload, ImagePresignedUrlKafkaEvent.class);
            ExternalImageView imageView = externalImageViewRepository.findByImageKey(event.key())
                    .orElseGet(() -> ExternalImageView.create(event.key(), event.presignedUrl(), event.exp()));

            imageView.update(event.presignedUrl(), event.exp());
            externalImageViewRepository.save(imageView);
            log.info("Successfully updated ExternalImageView for key: {}", event.key());
        } catch (Exception e) {
            log.error("Failed to handle ImagePresignedUrlKafkaEvent for payload: {}", payload, e);
            throw new RuntimeException("Failed to update ExternalImageView", e);
        }
    }
}
