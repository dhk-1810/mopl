package org.codeit.sb06.team03.mopl.content.application;

import io.awspring.cloud.s3.S3Exception;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.Content;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.contentTag.ContentTagService;
import org.codeit.sb06.team03.mopl.tag.repository.TagRepository;
import org.codeit.sb06.team03.mopl.tag.service.TagService;
import org.codeit.sb06.team03.mopl.content.application.in.CreateContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.in.DeleteContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.in.UpdateContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.out.LoadContentPort;
import org.codeit.sb06.team03.mopl.content.application.out.SaveContentPort;
import org.codeit.sb06.team03.mopl.content.domain.ContentService;
import org.codeit.sb06.team03.mopl.content.domain.exception.ContentNotFoundException;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentUpdateRequest;
import org.codeit.sb06.team03.mopl.s3.S3Service;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ContentCommandService implements CreateContentUseCase, UpdateContentUseCase, DeleteContentUseCase {

    private final LoadContentPort loadContentPort;
    private final SaveContentPort saveContentPort;
    private final ContentService contentService; // 단방향 참조
    private final TagService tagService; // 단방향 참조
    private final ContentTagService contentTagService; // 단방향 참조
    private final S3Service s3Service; // 단방향 참조

    @Override
    public ContentReadModel create(ContentCreateRequest request, MultipartFile thumbnail) {


        UUID thumbnailKey = UUID.randomUUID();
        Content content = contentService.create(
                request.type(), request.title(), request.description(), thumbnailKey);
        saveContentPort.save(content);
        try {
            s3Service.uploadFile(thumbnailKey.toString(), thumbnail);
        } catch(Exception e) {
            throw new S3Exception("업로드 실패", e);
        }
        Set<String> tags = contentTagService.create(content.getId(), request.tags());

        return ContentReadModel.from(content, tags);
    }

    @Override
    public ContentReadModel update(UUID contentId, ContentUpdateRequest request) {

        Content content = loadContentPort.findByIdWithTags(contentId)
                .orElseThrow(() -> ContentNotFoundException.fromId(contentId));
        contentService.update(content, request.title(), request.description());
        saveContentPort.save(content);

        Set<String> tags = contentTagService.create(content.getId(), request.tags());
        return ContentReadModel.from(content, tags);
    }

    @Override
    public void delete(UUID id) {
        saveContentPort.deleteById(id);
    }
}
