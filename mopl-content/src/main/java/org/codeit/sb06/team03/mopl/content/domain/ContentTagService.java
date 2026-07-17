package org.codeit.sb06.team03.mopl.content.domain;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.domain.entity.ContentTag;
import org.codeit.sb06.team03.mopl.content.repository.ContentRepository;
import org.codeit.sb06.team03.mopl.content.repository.ContentTagRepository;
import org.codeit.sb06.team03.mopl.tag.entity.Tag;
import org.codeit.sb06.team03.mopl.tag.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ContentTagService {

    // Tag, Content, ContentTag는 동일 DB 사용.
    private final ContentTagRepository contentTagRepository;
    private final ContentRepository contentRepository;
    private final TagRepository tagRepository;

    public Set<String> create(UUID contentId, Set<String> tags){

        Set<String> normalizedNames = tags.stream()
                .map(name -> name.trim().toLowerCase())
                .collect(Collectors.toSet());

        List<Tag> existingTags = tagRepository.findByNameIn(normalizedNames);
        Set<String> existingNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        List<Tag> newTags = normalizedNames.stream()
                .filter(name -> !existingNames.contains(name))
                .map(Tag::create)
                .toList();
        tagRepository.saveAll(newTags);

        Set<String> response = new HashSet<>();
        response.addAll(existingNames);
        response.addAll(newTags.stream().map(Tag::getName).collect(Collectors.toSet()));
        return response;
    }

    public Set<String> getByContentId(UUID contentId){
        List<ContentTag> contentTags = contentTagRepository.findByContentId(contentId);
        return contentTags.stream()
                .map(contentTag -> contentTag.getTag().getName()) // FETCH JOIN
                .collect(Collectors.toSet());
    }
}

