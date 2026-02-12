package org.codeit.sb06.team03.mopl.playlist.infra.out;

import org.codeit.sb06.team03.mopl.playlist.domain.entity.Curation;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.CurationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CurationRepository extends JpaRepository<Curation, Long> {

    boolean existsById(CurationId id);

    List<Curation> id(CurationId id);

    Optional<Curation> findById(CurationId id);

    void deleteById(CurationId id);
}
