package org.codeit.sb06.team03.mopl.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.S3Properties;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class S3Service {

    private final S3Properties properties;
    private final S3Presigner presigner;

    public String createPresignedUrl(String key, Duration timeout) {
        GetObjectPresignRequest presignedRequest = GetObjectPresignRequest.builder()
                .signatureDuration(timeout)
                .getObjectRequest(builder -> builder.bucket(properties.bucketName()).key(key))
                .build();
        return presigner.presignGetObject(presignedRequest).url().toString();
    }
}
