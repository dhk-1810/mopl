package org.codeit.sb06.team03.mopl.image.repository;

import org.codeit.sb06.team03.mopl.image.domain.entity.ExternalImageView;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class ExternalImageViewRepository {
    public Optional<ExternalImageView> findByImageKey(String imageKey) {
        return Optional.empty();
    }

    public List<ExternalImageView> findByImageKeyIn(List<String> imageKeys) {
        return Collections.emptyList();
    }
}
