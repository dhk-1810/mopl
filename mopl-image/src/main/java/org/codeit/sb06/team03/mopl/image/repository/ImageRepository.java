package org.codeit.sb06.team03.mopl.image.repository;

import org.codeit.sb06.team03.mopl.image.domain.TimeoutImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<TimeoutImage, UUID> {

    Optional<TimeoutImage> findByKey(String key);

    List<TimeoutImage> findByKeyIn(Collection<String> keys);

}
