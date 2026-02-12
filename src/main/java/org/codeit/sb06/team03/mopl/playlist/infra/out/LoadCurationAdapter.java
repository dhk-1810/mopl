package org.codeit.sb06.team03.mopl.playlist.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.playlist.application.out.LoadCurationPort;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.Curation;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.CurationId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class LoadCurationAdapter implements LoadCurationPort {

    private final CurationRepository repository;

    @Override
    public boolean existsById(CurationId id) {
        return repository.existsById(id);
    }

    @Override
    public Optional<Curation> findById(CurationId id) {
        return repository.findById(id);
    }
}
