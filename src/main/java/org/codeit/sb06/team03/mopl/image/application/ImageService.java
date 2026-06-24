package org.codeit.sb06.team03.mopl.image.application;

import org.codeit.sb06.team03.mopl.image.application.in.GetPresignedUrlUseCase;
import org.codeit.sb06.team03.mopl.image.application.in.RegisterImageUseCase;
import org.codeit.sb06.team03.mopl.image.application.out.LoadImagePort;
import org.codeit.sb06.team03.mopl.image.application.out.SaveImagePort;
import org.codeit.sb06.team03.mopl.image.domain.TimeoutImage;
import org.codeit.sb06.team03.mopl.image.domain.policy.ImageKeyGenerationPolicy;
import org.codeit.sb06.team03.mopl.image.domain.policy.PresignedUrlTimeoutPolicy;
import org.codeit.sb06.team03.mopl.s3.S3Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImageService implements RegisterImageUseCase, GetPresignedUrlUseCase {

    private final LoadImagePort loadImagePort;
    private final SaveImagePort saveImagePort;
    private final S3Service s3Service;
    private final ImageKeyGenerationPolicy imageKeyGenerationPolicy;
    private final PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy;

    public ImageService(
            LoadImagePort loadImagePort,
            SaveImagePort saveImagePort,
            S3Service s3Service,
            @Qualifier("imageUUIDImageKeyGenerationPolicy") ImageKeyGenerationPolicy imageKeyGenerationPolicy,
            @Qualifier("imageBasicPresignedUrlTimeoutPolicy") PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy
    ) {
        this.loadImagePort = loadImagePort;
        this.saveImagePort = saveImagePort;
        this.s3Service = s3Service;
        this.imageKeyGenerationPolicy = imageKeyGenerationPolicy;
        this.presignedUrlTimeoutPolicy = presignedUrlTimeoutPolicy;
    }

    @Override
    @Transactional
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
            saveImagePort.save(timeoutImage);
            return key;
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed", e);
        }
    }

    @Override
    @Transactional
    public String getPresignedUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        TimeoutImage timeoutImage = loadImagePort.findByKey(key)
                .orElseGet(() -> {
                    // 키는 존재하지만 DB에 캐싱이 안 된 비정상 상태 대비
                    Instant exp = presignedUrlTimeoutPolicy.createExp(Instant.now());
                    String presignedUrl = s3Service.createPresignedUrl(key, presignedUrlTimeoutPolicy.timeout());
                    TimeoutImage newImg = TimeoutImage.create(key, exp, presignedUrl);
                    saveImagePort.save(newImg);
                    return newImg;
                });

        if (timeoutImage.isExpired()) {
            Instant exp = presignedUrlTimeoutPolicy.createExp(Instant.now());
            String presignedUrl = s3Service.createPresignedUrl(timeoutImage.getKey(), presignedUrlTimeoutPolicy.timeout());
            timeoutImage.setExp(exp);
            timeoutImage.setPresignedUrl(presignedUrl);
            saveImagePort.save(timeoutImage);
        }

        return timeoutImage.getPresignedUrl();
    }

    @Override
    @Transactional
    public Map<String, String> getPresignedUrls(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }

        // 중복 및 Null/Blank 제거
        List<String> distinctKeys = keys.stream()
                .filter(k -> k != null && !k.isBlank())
                .distinct()
                .toList();

        List<TimeoutImage> timeoutImages = loadImagePort.findByKeys(distinctKeys);
        Map<String, TimeoutImage> imageMap = timeoutImages.stream()
                .collect(Collectors.toMap(TimeoutImage::getKey, img -> img));

        Map<String, String> result = new HashMap<>();

        for (String key : distinctKeys) {
            TimeoutImage timeoutImage = imageMap.get(key);
            if (timeoutImage == null) {
                // 키는 존재하지만 DB 캐시가 없는 상태 대비
                Instant exp = presignedUrlTimeoutPolicy.createExp(Instant.now());
                String presignedUrl = s3Service.createPresignedUrl(key, presignedUrlTimeoutPolicy.timeout());
                TimeoutImage newImg = TimeoutImage.create(key, exp, presignedUrl);
                saveImagePort.save(newImg);
                result.put(key, newImg.getPresignedUrl());
            } else {
                if (timeoutImage.isExpired()) {
                    Instant exp = presignedUrlTimeoutPolicy.createExp(Instant.now());
                    String presignedUrl = s3Service.createPresignedUrl(timeoutImage.getKey(), presignedUrlTimeoutPolicy.timeout());
                    timeoutImage.setExp(exp);
                    timeoutImage.setPresignedUrl(presignedUrl);
                    saveImagePort.save(timeoutImage);
                }
                result.put(key, timeoutImage.getPresignedUrl());
            }
        }

        return result;
    }
}

