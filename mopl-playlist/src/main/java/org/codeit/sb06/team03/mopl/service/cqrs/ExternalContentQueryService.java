package org.codeit.sb06.team03.mopl.service.cqrs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.client.ContentGrpcClient;
import org.codeit.sb06.team03.mopl.dto.response.ContentDto;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalContentView;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalContentViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(value = "playlistTransactionManager")
public class ExternalContentQueryService {

    private final ExternalContentViewRepository externalContentViewRepository;
    private final ContentGrpcClient contentGrpcClient;

    @Transactional(value = "playlistTransactionManager", readOnly = true)
    public List<ExternalContentView> getContents(Collection<UUID> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<UUID, ExternalContentView> existingMap = externalContentViewRepository.findAllById(contentIds).stream()
                .collect(Collectors.toMap(ExternalContentView::getId, Function.identity()));

        List<ExternalContentView> result = new ArrayList<>();
        for (UUID contentId : contentIds) {
            ExternalContentView view = existingMap.get(contentId);
            if (view == null) {
                view = fetchAndSaveContent(contentId);
            }
            if (view != null) {
                result.add(view);
            }
        }
        return result;
    }

    public ExternalContentView getContent(UUID contentId) {
        if (contentId == null) {
            return null;
        }
        return externalContentViewRepository.findById(contentId)
                .orElseGet(() -> fetchAndSaveContent(contentId));
    }

    private ExternalContentView fetchAndSaveContent(UUID contentId) {
        try {
            ContentDto contentDto = contentGrpcClient.getContentById(contentId);
            if (contentDto != null) {
                String tags = contentDto.tags() != null ? String.join(",", contentDto.tags()) : "";
                ExternalContentView view = ExternalContentView.create(
                        contentDto.id(),
                        contentDto.type(),
                        contentDto.title(),
                        contentDto.description(),
                        contentDto.thumbnailUrl(),
                        tags,
                        contentDto.averageRating(),
                        contentDto.reviewCount(),
                        contentDto.watcherCount()
                );
                return externalContentViewRepository.save(view);
            }
        } catch (Exception e) {
            log.error("Failed to fetch and save content via gRPC for contentId: {}", contentId, e);
        }
        return null;
    }
}
