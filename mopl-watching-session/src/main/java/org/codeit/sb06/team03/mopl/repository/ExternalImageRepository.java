package org.codeit.sb06.team03.mopl.repository;

import org.codeit.sb06.team03.mopl.entity.ExternalTimeoutImageView;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class ExternalImageRepository {
    public Optional<ExternalTimeoutImageView> findByKey(String key) {
        return Optional.empty();
    }

    public List<ExternalTimeoutImageView> findByKeyIn(List<String> keys) {
        return Collections.emptyList();
    }

    public ExternalTimeoutImageView save(ExternalTimeoutImageView entity) {
        return entity;
    }
}
