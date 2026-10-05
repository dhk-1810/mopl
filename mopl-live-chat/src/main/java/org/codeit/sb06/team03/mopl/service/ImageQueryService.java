package org.codeit.sb06.team03.mopl.service;

import org.codeit.sb06.team03.mopl.entity.ExternalTimeoutImageView;
import org.codeit.sb06.team03.mopl.entity.policy.PresignedUrlTimeoutPolicy;
import org.codeit.sb06.team03.mopl.repository.ExternalImageRepository;
import org.codeit.sb06.team03.mopl.s3.S3Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImageQueryService {

    private final ExternalImageRepository imageRepository;
    private final S3Service s3Service;
    private final PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy;

    public ImageQueryService(
            ExternalImageRepository imageRepository,
            S3Service s3Service,
            @Qualifier("imageBasicPresignedUrlTimeoutPolicy") PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy
    ) {
        this.imageRepository = imageRepository;
        this.s3Service = s3Service;
        this.presignedUrlTimeoutPolicy = presignedUrlTimeoutPolicy;
    }

    @Transactional
    public String getPresignedUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        if (key.startsWith("http://") || key.startsWith("https://")) {
            return key;
        }
        ExternalTimeoutImageView timeoutImage = imageRepository.findByKey(key)
                .orElseGet(() -> {
                    Instant exp = presignedUrlTimeoutPolicy.createExp(Instant.now());
                    String presignedUrl = s3Service.createPresignedUrl(key, presignedUrlTimeoutPolicy.timeout());
                    ExternalTimeoutImageView newImg = ExternalTimeoutImageView.create(key, exp, presignedUrl);
                    imageRepository.save(newImg);
                    return newImg;
                });

        if (timeoutImage.isExpired()) {
            Instant exp = presignedUrlTimeoutPolicy.createExp(Instant.now());
            String presignedUrl = s3Service.createPresignedUrl(timeoutImage.getKey(), presignedUrlTimeoutPolicy.timeout());
            timeoutImage.setExp(exp);
            timeoutImage.setPresignedUrl(presignedUrl);
            imageRepository.save(timeoutImage);
        }

        return timeoutImage.getPresignedUrl();
    }

    @Transactional
    public Map<String, String> getPresignedUrls(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> distinctKeys = keys.stream()
                .filter(k -> k != null && !k.isBlank())
                .distinct()
                .toList();

        List<String> s3Keys = distinctKeys.stream()
                .filter(k -> !k.startsWith("http://") && !k.startsWith("https://"))
                .toList();

        List<ExternalTimeoutImageView> timeoutImages = imageRepository.findByKeyIn(s3Keys);
        Map<String, ExternalTimeoutImageView> imageMap = timeoutImages.stream()
                .collect(Collectors.toMap(ExternalTimeoutImageView::getKey, img -> img));

        Map<String, String> result = new HashMap<>();

        for (String key : distinctKeys) {
            if (key.startsWith("http://") || key.startsWith("https://")) {
                result.put(key, key);
                continue;
            }
            ExternalTimeoutImageView timeoutImage = imageMap.get(key);
            if (timeoutImage == null) {
                Instant exp = presignedUrlTimeoutPolicy.createExp(Instant.now());
                String presignedUrl = s3Service.createPresignedUrl(key, presignedUrlTimeoutPolicy.timeout());
                ExternalTimeoutImageView newImg = ExternalTimeoutImageView.create(key, exp, presignedUrl);
                imageRepository.save(newImg);
                result.put(key, newImg.getPresignedUrl());
            } else {
                if (timeoutImage.isExpired()) {
                    Instant exp = presignedUrlTimeoutPolicy.createExp(Instant.now());
                    String presignedUrl = s3Service.createPresignedUrl(timeoutImage.getKey(), presignedUrlTimeoutPolicy.timeout());
                    timeoutImage.setExp(exp);
                    timeoutImage.setPresignedUrl(presignedUrl);
                    imageRepository.save(timeoutImage);
                }
                result.put(key, timeoutImage.getPresignedUrl());
            }
        }

        return result;
    }
}
