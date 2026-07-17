package org.codeit.sb06.team03.mopl.image.application;

import org.codeit.sb06.team03.mopl.image.domain.TimeoutImage;
import org.codeit.sb06.team03.mopl.image.domain.policy.PresignedUrlTimeoutPolicy;
import org.codeit.sb06.team03.mopl.image.infra.out.ImageRepository;
import org.codeit.sb06.team03.mopl.s3.S3Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImageQueryService {

    private final ImageRepository imageRepository;
    private final S3Service s3Service;
    private final PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy;

    public ImageQueryService(
            ImageRepository imageRepository,
            S3Service s3Service,
            @Qualifier("imageBasicPresignedUrlTimeoutPolicy") PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy
    ) {
        this.imageRepository = imageRepository;
        this.s3Service = s3Service;
        this.presignedUrlTimeoutPolicy = presignedUrlTimeoutPolicy;
    }

    @Transactional("imageTransactionManager")
    public String getPresignedUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        if (key.startsWith("http://") || key.startsWith("https://")) {
            return key;
        }
        TimeoutImage timeoutImage = imageRepository.findByKey(key)
                .orElseGet(() -> {
                    // 키는 존재하지만 DB에 캐싱이 안 된 비정상 상태 대비
                    Instant exp = presignedUrlTimeoutPolicy.createExp(Instant.now());
                    String presignedUrl = s3Service.createPresignedUrl(key, presignedUrlTimeoutPolicy.timeout());
                    TimeoutImage newImg = TimeoutImage.create(key, exp, presignedUrl);
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

    @Transactional("imageTransactionManager")
    public Map<String, String> getPresignedUrls(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }

        // 중복 및 Null/Blank 제거
        List<String> distinctKeys = keys.stream()
                .filter(k -> k != null && !k.isBlank())
                .distinct()
                .toList();

        // 외부 URL을 제외한 S3 key들만 DB 및 S3 조회 대상으로 필터링
        List<String> s3Keys = distinctKeys.stream()
                .filter(k -> !k.startsWith("http://") && !k.startsWith("https://"))
                .toList();

        List<TimeoutImage> timeoutImages = imageRepository.findByKeyIn(s3Keys);
        Map<String, TimeoutImage> imageMap = timeoutImages.stream()
                .collect(Collectors.toMap(TimeoutImage::getKey, img -> img));

        Map<String, String> result = new HashMap<>();

        for (String key : distinctKeys) {
            if (key.startsWith("http://") || key.startsWith("https://")) {
                result.put(key, key);
                continue;
            }
            TimeoutImage timeoutImage = imageMap.get(key);
            if (timeoutImage == null) {
                // 키는 존재하지만 DB 캐시가 없는 상태 대비
                Instant exp = presignedUrlTimeoutPolicy.createExp(Instant.now());
                String presignedUrl = s3Service.createPresignedUrl(key, presignedUrlTimeoutPolicy.timeout());
                TimeoutImage newImg = TimeoutImage.create(key, exp, presignedUrl);
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
