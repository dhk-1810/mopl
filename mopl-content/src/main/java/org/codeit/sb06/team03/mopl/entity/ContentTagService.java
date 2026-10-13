package org.codeit.sb06.team03.mopl.entity;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.exception.ContentNotFoundException;
import org.codeit.sb06.team03.mopl.repository.ContentRepository;
import org.codeit.sb06.team03.mopl.repository.ContentTagRepository;
import org.codeit.sb06.team03.mopl.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(value = "contentTransactionManager")
public class ContentTagService {

    private final ContentTagRepository contentTagRepository;
    private final ContentRepository contentRepository;
    private final TagRepository tagRepository;

    public Set<String> create(UUID contentId, Set<String> tags) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> ContentNotFoundException.fromId(contentId));

        if (tags == null || tags.isEmpty()) {
            List<ContentTag> current = contentTagRepository.findByContentId(contentId);
            if (!current.isEmpty()) {
                contentTagRepository.deleteAll(current);
            }
            return Collections.emptySet();
        }

        Set<String> normalizedNames = tags.stream()
                .filter(Objects::nonNull)
                .map(name -> name.trim().toLowerCase())
                .filter(name -> !name.isBlank())
                .collect(Collectors.toSet());

        if (normalizedNames.isEmpty()) {
            List<ContentTag> current = contentTagRepository.findByContentId(contentId);
            if (!current.isEmpty()) {
                contentTagRepository.deleteAll(current);
            }
            return Collections.emptySet();
        }

        List<Tag> existingTags = tagRepository.findByNameIn(normalizedNames);
        Map<String, Tag> tagMap = existingTags.stream()
                .collect(Collectors.toMap(Tag::getName, Function.identity(), (a, b) -> a));

        List<Tag> newTags = normalizedNames.stream()
                .filter(name -> !tagMap.containsKey(name))
                .map(Tag::create)
                .toList();

        if (!newTags.isEmpty()) {
            List<Tag> savedNewTags = tagRepository.saveAll(newTags);
            for (Tag tag : savedNewTags) {
                tagMap.put(tag.getName(), tag);
            }
        }

        List<ContentTag> currentContentTags = contentTagRepository.findByContentId(contentId);
        Set<String> currentTagNames = currentContentTags.stream()
                .map(ct -> ct.getTag().getName())
                .collect(Collectors.toSet());

        List<ContentTag> toDelete = currentContentTags.stream()
                .filter(ct -> !normalizedNames.contains(ct.getTag().getName()))
                .toList();
        if (!toDelete.isEmpty()) {
            contentTagRepository.deleteAll(toDelete);
        }

        List<ContentTag> toInsert = normalizedNames.stream()
                .filter(name -> !currentTagNames.contains(name))
                .map(name -> ContentTag.create(content, tagMap.get(name)))
                .toList();
        if (!toInsert.isEmpty()) {
            contentTagRepository.saveAll(toInsert);
        }

        return normalizedNames;
    }

    @Transactional(value = "contentTransactionManager", readOnly = true)
    public Set<String> getByContentId(UUID contentId) {
        List<ContentTag> contentTags = contentTagRepository.findByContentId(contentId);
        return contentTags.stream()
                .map(contentTag -> contentTag.getTag().getName())
                .collect(Collectors.toSet());
    }
}
