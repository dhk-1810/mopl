package org.codeit.sb06.team03.mopl.service.application.cqrs;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalContentView;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalContentViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RequiredArgsConstructor
@Service
@Transactional(value = "playlistTransactionManager", readOnly = true)
public class ExternalContentQueryService {

    private final ExternalContentViewRepository externalContentViewRepository;

    public List<ExternalContentView> getContents(Collection<UUID> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return Collections.emptyList();
        }
        return externalContentViewRepository.findAllById(contentIds);
    }

    public ExternalContentView getContent(UUID contentId) {
        return externalContentViewRepository.findById(contentId)
                .orElse(null);
    }
}
