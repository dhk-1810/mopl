package org.codeit.sb06.team03.mopl.repository;

import io.github.openfeign.querydsl.jpa.spring.repository.QuerydslJpaRepository;
import org.codeit.sb06.team03.mopl.entity.Curation;
import org.codeit.sb06.team03.mopl.entity.CurationId;
import org.codeit.sb06.team03.mopl.entity.QCuration;

import java.util.*;
import java.util.stream.Collectors;

public interface CurationRepository extends QuerydslJpaRepository<Curation, CurationId> {

    boolean existsById(CurationId id);

    List<Curation> id(CurationId id);

    Optional<Curation> findById(CurationId id);

    void deleteById(CurationId id);

    default List<UUID> findAllByPlaylistId(UUID playlistId) {
        QCuration curation = QCuration.curation;
        return select(curation.id.contentId)
                .where(curation.deletedAt.isNull().and(curation.id.playlistId.eq(playlistId)))
                .from(curation)
                .fetch();
    }

    default void deleteAllByPlaylistId(UUID playlistId) {
        QCuration curation = QCuration.curation;
        update(curation)
                .set(curation.deletedAt, java.time.Instant.now())
                .where(curation.id.playlistId.eq(playlistId))
                .execute();
    }

    default void deleteAllByContentId(UUID contentId) {
        QCuration curation = QCuration.curation;
        update(curation)
                .set(curation.deletedAt, java.time.Instant.now())
                .where(curation.id.contentId.eq(contentId))
                .execute();
    }

    default void restoreAllByContentId(UUID contentId) {
        QCuration curation = QCuration.curation;
        update(curation)
                .setNull(curation.deletedAt)
                .where(curation.id.contentId.eq(contentId))
                .execute();
    }

    default int purgeDeletedBefore(java.time.Instant threshold) {
        QCuration curation = QCuration.curation;
        return (int) delete(curation)
                .where(curation.deletedAt.isNotNull().and(curation.deletedAt.before(threshold)))
                .execute();
    }

    default List<Curation> findAllByContentId(UUID contentId) {
        QCuration curation = QCuration.curation;
        return select(curation)
                .from(curation)
                .where(curation.deletedAt.isNull().and(curation.id.contentId.eq(contentId)))
                .fetch();
    }

    default Map<UUID, List<UUID>> findAllByPlaylistIdsIn(Set<UUID> playlistIds) {
        if (playlistIds == null || playlistIds.isEmpty()) {
            return Collections.emptyMap();
        }

        QCuration curation = QCuration.curation;
        List<Curation> curations = select(curation)
                .from(curation)
                .where(curation.deletedAt.isNull().and(curation.id.playlistId.in(playlistIds)))
                .fetch();

        return curations.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getId().getPlaylistId(),
                        Collectors.mapping(c -> c.getId().getContentId(), Collectors.toList())
                ));
    }
}
