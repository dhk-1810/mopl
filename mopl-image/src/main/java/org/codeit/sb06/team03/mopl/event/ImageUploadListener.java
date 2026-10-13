package org.codeit.sb06.team03.mopl.event;

import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.entity.TimeoutImage;
import org.codeit.sb06.team03.mopl.entity.policy.PresignedUrlTimeoutPolicy;
import org.codeit.sb06.team03.mopl.repository.ImageRepository;
import org.codeit.sb06.team03.mopl.service.S3Service;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
public class ImageUploadListener {

    private final ImageRepository imageRepository;
    private final PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy;
    private final S3Service s3Service;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ImageUploadListener(
            S3Service s3Service,
            ImageRepository imageRepository,
            @Qualifier("imageBasicPresignedUrlTimeoutPolicy") PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy,
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.s3Service = s3Service;
        this.imageRepository = imageRepository;
        this.presignedUrlTimeoutPolicy = presignedUrlTimeoutPolicy;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional("imageTransactionManager")
    @RabbitListener(queues = RabbitConfig.IMAGE_UPLOAD_QUEUE)
    public void handleImageUpload(ImageUploadEvent event) {
        log.info("Received image metadata registration event via RabbitMQ. key: {}", event.key());
        try {
            // 1. S3 Presigned URL 생성 및 이미지 DB 저장
            Instant exp = presignedUrlTimeoutPolicy.createExp(Instant.now());
            String presignedUrl = s3Service.createPresignedUrl(event.key(), presignedUrlTimeoutPolicy.timeout());
            TimeoutImage timeoutImage = TimeoutImage.create(event.key(), exp, presignedUrl);
            
            imageRepository.save(timeoutImage);
            log.info("Successfully created cache metadata for pre-uploaded S3 key: {}", event.key());

            // 2. Presigned URL 생성 완료 이벤트를 Kafka로 브로드캐스팅 (User 등 타 서비스 복제뷰 갱신용)
            ImagePresignedUrlKafkaEvent urlCreatedEvent = new ImagePresignedUrlKafkaEvent(
                    event.key(),
                    presignedUrl,
                    exp
            );
            kafkaTemplate.send(
                    org.codeit.sb06.team03.mopl.config.KafkaConfig.TOPIC_IMAGE_PRESIGNED_URL_EVENTS,
                    event.key(),
                    urlCreatedEvent
            );
            log.info("Published ImagePresignedUrlKafkaEvent to Kafka for key: {}", event.key());
        } catch (Exception e) {
            log.error("Failed to process async metadata cache creation for key: {}", event.key(), e);
            throw new RuntimeException("Async metadata cache creation failed", e);
        }
    }
}
