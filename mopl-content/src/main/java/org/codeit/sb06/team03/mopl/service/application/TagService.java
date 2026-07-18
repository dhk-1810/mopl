package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.domain.entity.Tag;
import org.codeit.sb06.team03.mopl.exception.TagNotFoundException;
import org.codeit.sb06.team03.mopl.repository.TagRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TagService {

    private final TagRepository repository;

    public Tag create(String name) {
        return Tag.create(name);
    }
}
