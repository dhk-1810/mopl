package org.codeit.sb06.team03.mopl.content.domain;

import org.codeit.sb06.team03.mopl.content.domain.entity.Tag;
import org.springframework.stereotype.Service;

@Service
public class TagService {

    public Tag create(String name) {
        return Tag.create(name);
    }
}
