package org.codeit.sb06.team03.mopl.repository;

import org.codeit.sb06.team03.mopl.entity.ExternalTimeoutImageView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExternalImageRepository extends JpaRepository<ExternalTimeoutImageView, UUID> {
    Optional<ExternalTimeoutImageView> findByKey(String key);
    List<ExternalTimeoutImageView> findByKeyIn(List<String> keys);
}
