package org.codeit.sb06.team03.mopl.client;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.codeit.sb06.team03.mopl.grpc.image.GetPresignedUrlRequest;
import org.codeit.sb06.team03.mopl.grpc.image.GetPresignedUrlResponse;
import org.codeit.sb06.team03.mopl.grpc.image.ImageServiceGrpc;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class ImageGrpcClient {

    @GrpcClient("image-service")
    private ImageServiceGrpc.ImageServiceBlockingStub imageServiceStub;

    public record ImageDto(
            String key,
            String presignedUrl,
            Instant exp
    ) {}

    public ImageDto getPresignedUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        try {
            GetPresignedUrlRequest request = GetPresignedUrlRequest.newBuilder()
                    .setKey(key)
                    .build();

            GetPresignedUrlResponse response = imageServiceStub.getPresignedUrl(request);

            Instant exp = null;
            if (response.getExp() != null && !response.getExp().isBlank()) {
                try {
                    exp = Instant.parse(response.getExp());
                } catch (Exception ignored) {}
            }

            return new ImageDto(
                    response.getKey(),
                    response.getPresignedUrl(),
                    exp
            );
        } catch (Exception e) {
            log.warn("Failed to fetch presigned URL via gRPC for key: {}, error: {}", key, e.getMessage());
            return null;
        }
    }
}
