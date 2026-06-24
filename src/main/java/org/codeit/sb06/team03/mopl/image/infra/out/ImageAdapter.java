package org.codeit.sb06.team03.mopl.image.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.image.application.out.LoadImagePort;
import org.codeit.sb06.team03.mopl.image.application.out.SaveImagePort;
import org.codeit.sb06.team03.mopl.image.domain.TimeoutImage;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ImageAdapter implements LoadImagePort, SaveImagePort {

    private final ImageRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Optional<TimeoutImage> findByKey(String key) {
        return repository.findByKey(key);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeoutImage> findByKeys(List<String> keys) {
        return repository.findByKeyIn(keys);
    }

    @Override
    @Transactional
    public void save(TimeoutImage timeoutImage) {
        repository.save(timeoutImage);
    }

    @Override
    @Transactional
    public void deleteByKey(String key) {
        repository.findByKey(key).ifPresent(repository::delete);
    }
}

