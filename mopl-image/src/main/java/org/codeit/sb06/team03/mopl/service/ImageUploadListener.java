package org.codeit.sb06.team03.mopl.service;

import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.entity.TimeoutImage;
import org.codeit.sb06.team03.mopl.entity.policy.PresignedUrlTimeoutPolicy;
import org.codeit.sb06.team03.mopl.repository.ImageRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
public class ImageUploadListener {

    private final S3Service s3Service;
    private final ImageRepository imageRepository;
    private final PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy;

    public ImageUploadListener(
            S3Service s3Service,
            ImageRepository imageRepository,
            @Qualifier("imageBasicPresignedUrlTimeoutPolicy") PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy
    ) {
        this.s3Service = s3Service;
        this.imageRepository = imageRepository;
        this.presignedUrlTimeoutPolicy = presignedUrlTimeoutPolicy;
    }

    @Transactional("imageTransactionManager")
    @RabbitListener(queues = "mopl.image.queue.upload")
    public void handleImageUpload(ImageUploadEvent event) {
        log.info("Received image metadata registration event via RabbitMQ. key: {}", event.key());
        try {
            // S3 직접 업로드는 이미 유저 서비스에서 수행 완료함.
            // 이미지 서비스에서는 DB 캐시(TimeoutImage) 데이터 생성만 신속히 진행함.
            Instant exp = presignedUrlTimeoutPolicy.createExp(Instant.now());
            String presignedUrl = s3Service.createPresignedUrl(event.key(), presignedUrlTimeoutPolicy.timeout());
            TimeoutImage timeoutImage = TimeoutImage.create(event.key(), exp, presignedUrl);
            
            imageRepository.save(timeoutImage);
            log.info("Successfully created cache metadata for pre-uploaded S3 key: {}", event.key());
        } catch (Exception e) {
            log.error("Failed to process async metadata cache creation for key: {}", event.key(), e);
            throw new RuntimeException("Async metadata cache creation failed", e);
        }
    }
}
