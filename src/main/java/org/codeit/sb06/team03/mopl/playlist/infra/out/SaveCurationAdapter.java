package org.codeit.sb06.team03.mopl.playlist.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.playlist.application.out.SaveCurationPort;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.Curation;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.CurationId;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SaveCurationAdapter implements SaveCurationPort {

    private final CurationRepository curationRepository;

    @Override
    public void save(Curation curation) {
        curationRepository.save(curation);
    }

    @Override
    public void delete(CurationId id) {
        curationRepository.deleteById(id);
    }
}
