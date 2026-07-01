package org.codeit.sb06.team03.mopl.image.infra.out;

import org.codeit.sb06.team03.mopl.image.domain.TimeoutImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<TimeoutImage, UUID> {

    Optional<TimeoutImage> findByKey(String key);

    List<TimeoutImage> findByKeyIn(Collection<String> keys);

}
