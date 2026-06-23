package org.codeit.sb06.team03.mopl.user.infra.out;

import org.codeit.sb06.team03.mopl.s3.S3Service;
import org.codeit.sb06.team03.mopl.user.domain.exception.ImageRegistrationFailedException;
import org.codeit.sb06.team03.mopl.user.domain.policy.ImageKeyGenerationPolicy;
import org.codeit.sb06.team03.mopl.user.domain.policy.ImageRegistrationPolicy;
import org.codeit.sb06.team03.mopl.user.domain.policy.PresignedUrlTimeoutPolicy;
import org.codeit.sb06.team03.mopl.user.domain.vo.TimeoutImage;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

@Component
public class S3ImageRegistrationPolicy implements ImageRegistrationPolicy {

    private final S3Service s3Service;
    private final ImageKeyGenerationPolicy imageKeyGenerationPolicy;
    private final PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy;

    public S3ImageRegistrationPolicy(
            S3Service s3Service,
            ImageKeyGenerationPolicy imageKeyGenerationPolicy,
            PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy
    ) {
        this.s3Service = s3Service;
        this.imageKeyGenerationPolicy = imageKeyGenerationPolicy;
        this.presignedUrlTimeoutPolicy = presignedUrlTimeoutPolicy;
    }

    @Override
    public TimeoutImage register(MultipartFile image) {
        try {
            String key = imageKeyGenerationPolicy.generate();
            s3Service.uploadFile(key, image);
            Instant exp = presignedUrlTimeoutPolicy.createExp(Instant.now());
            String presignedUrl = s3Service.createPresignedUrl(key, presignedUrlTimeoutPolicy.timeout());
            return TimeoutImage.create(key, exp, presignedUrl);
        } catch (IOException e) {
            throw new ImageRegistrationFailedException(image, e);
        }
    }
}
