package org.codeit.sb06.team03.mopl.service.cqrs;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.ExternalImageView;
import org.codeit.sb06.team03.mopl.repository.ExternalImageViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ExternalImageQueryService {

    private final ExternalImageViewRepository externalImageViewRepository;

    public String getPresignedUrl(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            return null;
        }
        if (imageKey.startsWith("http://") || imageKey.startsWith("https://")) {
            return imageKey;
        }
        return externalImageViewRepository.findByImageKey(imageKey)
                .map(ExternalImageView::getPresignedUrl)
                .orElse(imageKey);
    }

    public Map<String, String> getPresignedUrls(List<String> imageKeys) {
        if (imageKeys == null || imageKeys.isEmpty()) {
            return Map.of();
        }
        List<ExternalImageView> images = externalImageViewRepository.findByImageKeyIn(imageKeys);
        Map<String, String> dbMap = images.stream()
                .collect(Collectors.toMap(
                        ExternalImageView::getImageKey,
                        ExternalImageView::getPresignedUrl,
                        (existing, replacement) -> existing
                ));

        return imageKeys.stream()
                .filter(key -> key != null && !key.isBlank())
                .collect(Collectors.toMap(
                        key -> key,
                        key -> {
                            if (key.startsWith("http://") || key.startsWith("https://")) {
                                return key;
                            }
                            return dbMap.getOrDefault(key, key);
                        },
                        (existing, replacement) -> existing
                ));
    }
}
