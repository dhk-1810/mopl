package org.codeit.sb06.team03.mopl.service;

import org.codeit.sb06.team03.mopl.entity.policy.PresignedUrlTimeoutPolicy;
import org.codeit.sb06.team03.mopl.entity.ExternalTimeoutImageView;
import org.codeit.sb06.team03.mopl.s3.S3Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component("userPresignedUrlUpdateListener")
public class PresignedUrlUpdateListener {

    private final S3Service s3Service;
    private final PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy;

    public PresignedUrlUpdateListener(
            S3Service s3Service,
            @Qualifier("imageBasicPresignedUrlTimeoutPolicy") PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy
    ) {
        this.s3Service = s3Service;
        this.presignedUrlTimeoutPolicy = presignedUrlTimeoutPolicy;
    }

    public void updatePresignedUrl(Object entity) {
        if (entity instanceof ExternalTimeoutImageView timeoutImage) {
            if (timeoutImage.isExpired()) {
                Instant exp = presignedUrlTimeoutPolicy.createExp(Instant.now());
                String presignedUrl = s3Service.createPresignedUrl(timeoutImage.getKey(), presignedUrlTimeoutPolicy.timeout());
                timeoutImage.setExp(exp);
                timeoutImage.setPresignedUrl(presignedUrl);
            }
        }
    }
}
