package org.codeit.sb06.team03.mopl.content.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.Content;
import org.codeit.sb06.team03.mopl.content.application.out.SaveContentPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class SaveContentAdapter implements SaveContentPort {

    private final ContentRepository repository;

    @Override
    public void save(Content content) {
        repository.save(content);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
