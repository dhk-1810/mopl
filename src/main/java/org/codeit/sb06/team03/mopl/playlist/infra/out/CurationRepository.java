package org.codeit.sb06.team03.mopl.playlist.infra.out;

import io.github.openfeign.querydsl.jpa.spring.repository.QuerydslJpaRepository;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.Curation;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.CurationId;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.QCuration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CurationRepository extends QuerydslJpaRepository<Curation, CurationId> {

    boolean existsById(CurationId id);

    List<Curation> id(CurationId id);

    Optional<Curation> findById(CurationId id);

    void deleteById(CurationId id);

    default void deleteAllByPlaylistId(UUID playlistId) {
        QCuration curation = QCuration.curation;
        delete(curation)
                .where(curation.id.playlistId.eq(playlistId))
                .execute();
    }
}
