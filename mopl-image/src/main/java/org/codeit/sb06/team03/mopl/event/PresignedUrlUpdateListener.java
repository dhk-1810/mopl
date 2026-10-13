package org.codeit.sb06.team03.mopl.event;

import jakarta.persistence.PostLoad;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.policy.PresignedUrlTimeoutPolicy;
import org.codeit.sb06.team03.mopl.entity.TimeoutImage;
import org.codeit.sb06.team03.mopl.service.S3Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Instant;

@RequiredArgsConstructor
@Component
public class PresignedUrlUpdateListener {

    private final S3Service s3Service;
    private final PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy;

    @PostLoad
    public void updatePresignedUrl(Object entity) {
        if (entity instanceof TimeoutImage timeoutImage) {
            if (timeoutImage.isExpired()) {
                Instant exp = presignedUrlTimeoutPolicy.createExp(Instant.now());
                String presignedUrl = s3Service.createPresignedUrl(timeoutImage.getKey(), presignedUrlTimeoutPolicy.timeout());
                timeoutImage.setExp(exp);
                timeoutImage.setPresignedUrl(presignedUrl);
            }
        }
    }
}
