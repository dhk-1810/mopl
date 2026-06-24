package org.codeit.sb06.team03.mopl.image.application.out;

import org.codeit.sb06.team03.mopl.image.domain.TimeoutImage;

import java.util.List;
import java.util.Optional;

public interface LoadImagePort {

    Optional<TimeoutImage> findByKey(String key);

    List<TimeoutImage> findByKeys(List<String> keys);

}
