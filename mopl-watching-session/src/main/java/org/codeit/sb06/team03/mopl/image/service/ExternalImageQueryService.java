package org.codeit.sb06.team03.mopl.image.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.image.domain.entity.ExternalImageView;
import org.codeit.sb06.team03.mopl.image.repository.ExternalImageViewRepository;
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
        return externalImageViewRepository.findByImageKey(imageKey)
                .map(ExternalImageView::getPresignedUrl)
                .orElse(null);
    }

    public Map<String, String> getPresignedUrls(List<String> imageKeys) {
        List<ExternalImageView> images = externalImageViewRepository.findByImageKeyIn(imageKeys);
        return images.stream()
                .collect(Collectors.toMap(
                        ExternalImageView::getImageKey,
                        ExternalImageView::getPresignedUrl,
                        (existing, replacement) -> existing
                ));
    }
}
