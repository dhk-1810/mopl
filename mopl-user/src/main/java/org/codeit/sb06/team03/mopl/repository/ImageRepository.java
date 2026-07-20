package org.codeit.sb06.team03.mopl.repository;

import org.codeit.sb06.team03.mopl.entity.TimeoutImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<TimeoutImage, UUID> {
    Optional<TimeoutImage> findByKey(String key);
    List<TimeoutImage> findByKeyIn(List<String> keys);
}
