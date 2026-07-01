package org.codeit.sb06.team03.mopl.tag.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.tag.entity.Tag;
import org.codeit.sb06.team03.mopl.tag.exception.TagNotFoundException;
import org.codeit.sb06.team03.mopl.tag.repository.TagRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TagService {

    private final TagRepository repository;

    public Tag create(String name) {
        return Tag.create(name);
    }
}
