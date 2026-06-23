package org.codeit.sb06.team03.mopl.content.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.Content;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.tag.repository.TagRepository;
import org.codeit.sb06.team03.mopl.tag.service.TagService;
import org.codeit.sb06.team03.mopl.content.application.in.CreateContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.in.DeleteContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.in.UpdateContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.out.LoadContentPort;
import org.codeit.sb06.team03.mopl.content.application.out.SaveContentPort;
import org.codeit.sb06.team03.mopl.content.domain.ContentService;
import org.codeit.sb06.team03.mopl.tag.entity.Tag;
import org.codeit.sb06.team03.mopl.content.domain.exception.ContentNotFoundException;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentUpdateRequest;
import org.codeit.sb06.team03.mopl.s3.S3Service;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ContentCommandService implements CreateContentUseCase, UpdateContentUseCase, DeleteContentUseCase {

    private final LoadContentPort loadContentPort;
    private final SaveContentPort saveContentPort;
    private final ContentService contentService; // 단방향 참조
    private final TagService tagService;
    private final S3Service s3Service;
    private final TagRepository tagRepository;

    @Override
    public ContentReadModel create(ContentCreateRequest request, MultipartFile thumbnail) {

        Set<String> normalizedNames = request.tags().stream()
                .map(name -> name.trim().toLowerCase())
                .collect(Collectors.toSet());

        List<Tag> existingTags = tagRepository.findByNameIn(normalizedNames);
        Set<String> existingNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        List<Tag> newTags = normalizedNames.stream()
                .filter(name -> !existingNames.contains(name))
                .map(Tag::create) // Tag.create(name)
                .map(tagRepository::save)
                .toList();

        List<Tag> allTags = new ArrayList<>();
        allTags.addAll(existingTags);
        allTags.addAll(newTags);

        s3Service.uploadFile(, thumbnail);
        String key = UUID.randomUUID().toString(); // TODO
        Content content = contentService.create(
                request.type(), request.title(), request.description(), key);
        saveContentPort.save(content);

        return ContentReadModel.from(content);
    }

    @Override
    public ContentReadModel update(UUID contentId, ContentUpdateRequest request) {

        Content content = loadContentPort.findByIdWithTags(contentId)
                .orElseThrow(() -> ContentNotFoundException.fromId(contentId));
        contentService.update(content, request.title(), request.description());
        saveContentPort.save(content);
        return ContentReadModel.from(content);
    }

    @Override
    public void delete(UUID id) {
        saveContentPort.deleteById(id);
    }
}
