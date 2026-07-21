package org.codeit.sb06.team03.mopl.image.repository;

import org.codeit.sb06.team03.mopl.image.domain.entity.ExternalImageView;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExternalImageViewRepository extends JpaRepository<ExternalImageView, UUID> {
    Optional<ExternalImageView> findByImageKey(String imageKey);
    List<ExternalImageView> findByImageKeyIn(List<String> imageKeys);
}
