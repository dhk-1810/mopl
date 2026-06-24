package org.codeit.sb06.team03.mopl.image.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.image.application.out.SaveImagePort;
import org.codeit.sb06.team03.mopl.image.domain.TimeoutImage;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class SaveImageAdapter implements SaveImagePort {

    private final ImageRepository imageRepository;

    @Override
    @Transactional
    public void save(TimeoutImage timeoutImage) {
        imageRepository.save(timeoutImage);
    }

    @Override
    @Transactional
    public void deleteByKey(String key) {
        imageRepository.findByKey(key).ifPresent(imageRepository::delete);
    }
}
