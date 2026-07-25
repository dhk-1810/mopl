package org.codeit.sb06.team03.mopl.service.cqrs;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalImageView;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalImageViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(value = "playlistTransactionManager", readOnly = true)
public class ExternalImageQueryService {

    private final ExternalImageViewRepository externalImageViewRepository;

    public String getPresignedUrl(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            return null;
        }
        return externalImageViewRepository.findByImageKey(imageKey)
                .map(ExternalImageView::getPresignedUrl)
                .orElse(null);
    }

    public Map<String, String> getPresignedUrls(List<String> imageKeys) {
        if (imageKeys == null || imageKeys.isEmpty()) {
            return Map.of();
        }
        List<ExternalImageView> images = externalImageViewRepository.findByImageKeyIn(imageKeys);
        return images.stream()
                .collect(Collectors.toMap(
                        ExternalImageView::getImageKey,
                        ExternalImageView::getPresignedUrl,
                        (existing, replacement) -> existing
                ));
    }
}
