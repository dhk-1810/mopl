package org.codeit.sb06.team03.mopl.repository.cqrs;

import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalImageView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExternalImageViewRepository extends JpaRepository<ExternalImageView, UUID> {
    Optional<ExternalImageView> findByImageKey(String imageKey);
    List<ExternalImageView> findByImageKeyIn(List<String> imageKeys);
}

