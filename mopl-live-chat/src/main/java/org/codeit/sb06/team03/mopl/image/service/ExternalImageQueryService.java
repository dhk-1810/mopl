package org.codeit.sb06.team03.mopl.image.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.client.ImageGrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExternalImageQueryService {

    private final ImageGrpcClient imageGrpcClient;

    public String getPresignedUrl(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            return null;
        }
        if (imageKey.startsWith("http://") || imageKey.startsWith("https://")) {
            return imageKey;
        }

        ImageGrpcClient.ImageDto imageDto = imageGrpcClient.getPresignedUrl(imageKey);
        if (imageDto != null && imageDto.presignedUrl() != null && !imageDto.presignedUrl().isBlank()) {
            return imageDto.presignedUrl();
        }

        return imageKey;
    }

    public Map<String, String> getPresignedUrls(List<String> imageKeys) {
        if (imageKeys == null || imageKeys.isEmpty()) {
            return Map.of();
        }
        return imageKeys.stream()
                .filter(key -> key != null && !key.isBlank())
                .distinct()
                .collect(Collectors.toMap(
                        key -> key,
                        this::getPresignedUrl,
                        (existing, replacement) -> existing
                ));
    }
}
