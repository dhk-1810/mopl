package org.codeit.sb06.team03.mopl.service;

import org.codeit.sb06.team03.mopl.entity.TimeoutImage;
import org.codeit.sb06.team03.mopl.entity.policy.ImageKeyGenerationPolicy;
import org.codeit.sb06.team03.mopl.entity.policy.PresignedUrlTimeoutPolicy;
import org.codeit.sb06.team03.mopl.repository.ImageRepository;
import org.codeit.sb06.team03.mopl.s3.S3Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

@Service
public class ImageCommandService {

    private final ImageRepository imageRepository;
    private final S3Service s3Service;
    private final ImageKeyGenerationPolicy imageKeyGenerationPolicy;
    private final PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy;

    public ImageCommandService(
            ImageRepository imageRepository,
            S3Service s3Service,
            @Qualifier("imageUUIDImageKeyGenerationPolicy") ImageKeyGenerationPolicy imageKeyGenerationPolicy,
            @Qualifier("imageBasicPresignedUrlTimeoutPolicy") PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy
    ) {
        this.imageRepository = imageRepository;
        this.s3Service = s3Service;
        this.imageKeyGenerationPolicy = imageKeyGenerationPolicy;
        this.presignedUrlTimeoutPolicy = presignedUrlTimeoutPolicy;
    }

    @Transactional("imageTransactionManager")
    public String register(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        try {
            String key = imageKeyGenerationPolicy.generate();
            s3Service.uploadFile(key, image);
            Instant exp = presignedUrlTimeoutPolicy.createExp(Instant.now());
            String presignedUrl = s3Service.createPresignedUrl(key, presignedUrlTimeoutPolicy.timeout());
            TimeoutImage timeoutImage = TimeoutImage.create(key, exp, presignedUrl);
            imageRepository.save(timeoutImage);
            return key;
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed", e);
        }
    }

    @Transactional("imageTransactionManager")
    public void deleteByKey(String key) {
        imageRepository.findByKey(key).ifPresent(imageRepository::delete);
    }
}
