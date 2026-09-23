package org.codeit.sb06.team03.mopl.service.cqrs;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dto.response.ContentDto;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalContentView;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalContentViewRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class ExternalContentWriter {

    private final ExternalContentViewRepository externalContentViewRepository;

    @Transactional(value = "playlistTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public ExternalContentView saveFromDto(ContentDto contentDto) {
        if (contentDto == null) {
            return null;
        }
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
}
