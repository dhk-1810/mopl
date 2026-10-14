package org.codeit.sb06.team03.mopl.service.cqrs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.client.ImageGrpcClient;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalImageView;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalImageViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExternalImageQueryService {

    private final ExternalImageViewRepository externalImageViewRepository;
    private final ImageGrpcClient imageGrpcClient;

    @Transactional(value = "playlistTransactionManager")
    public String getPresignedUrl(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            return null;
        }
        if (imageKey.startsWith("http://") || imageKey.startsWith("https://")) {
            return imageKey;
        }

        Optional<ExternalImageView> optionalView = externalImageViewRepository.findByImageKey(imageKey);
        if (optionalView.isPresent()) {
            ExternalImageView view = optionalView.get();
            if (!view.isExpired() && view.getPresignedUrl() != null && !view.getPresignedUrl().isBlank()) {
                return view.getPresignedUrl();
            }
        }

        return fetchAndSavePresignedUrl(imageKey, optionalView.orElse(null));
    }

    private String fetchAndSavePresignedUrl(String imageKey, ExternalImageView existing) {
        ImageGrpcClient.ImageDto imageDto = imageGrpcClient.getPresignedUrl(imageKey);
        if (imageDto == null || imageDto.presignedUrl() == null || imageDto.presignedUrl().isBlank()) {
            return existing != null ? existing.getPresignedUrl() : imageKey;
        }

        try {
            if (existing != null) {
                existing.update(imageDto.presignedUrl(), imageDto.exp());
                externalImageViewRepository.save(existing);
            } else {
                ExternalImageView newView = ExternalImageView.create(imageKey, imageDto.presignedUrl(), imageDto.exp());
                externalImageViewRepository.save(newView);
            }
        } catch (Exception e) {
            log.warn("Failed to cache replicated ExternalImageView for key: {}, error: {}", imageKey, e.getMessage());
        }

        return imageDto.presignedUrl();
    }

    @Transactional(value = "playlistTransactionManager")
    public Map<String, String> getPresignedUrls(List<String> imageKeys) {
        if (imageKeys == null || imageKeys.isEmpty()) {
            return Map.of();
        }
        List<ExternalImageView> images = externalImageViewRepository.findByImageKeyIn(imageKeys);
        Map<String, ExternalImageView> viewMap = images.stream()
                .collect(Collectors.toMap(
                        ExternalImageView::getImageKey,
                        v -> v,
                        (existing, replacement) -> existing
                ));

        return imageKeys.stream()
                .filter(key -> key != null && !key.isBlank())
                .distinct()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> {
                            if (key.startsWith("http://") || key.startsWith("https://")) {
                                return key;
                            }
                            ExternalImageView view = viewMap.get(key);
                            if (view != null && !view.isExpired() && view.getPresignedUrl() != null && !view.getPresignedUrl().isBlank()) {
                                return view.getPresignedUrl();
                            }
                            return fetchAndSavePresignedUrl(key, view);
                        },
                        (existing, replacement) -> existing
                ));
    }
}
