package org.codeit.sb06.team03.mopl.image.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.image.application.out.LoadImagePort;
import org.codeit.sb06.team03.mopl.image.domain.TimeoutImage;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class LoadImageAdapter implements LoadImagePort {

    private final ImageRepository imageRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<TimeoutImage> findByKey(String key) {
        return imageRepository.findByKey(key);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeoutImage> findByKeys(List<String> keys) {
        return imageRepository.findByKeyIn(keys);
    }
}
