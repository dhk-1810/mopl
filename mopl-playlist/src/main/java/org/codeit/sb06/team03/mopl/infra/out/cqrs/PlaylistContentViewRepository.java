package org.codeit.sb06.team03.mopl.infra.out.cqrs;

import org.codeit.sb06.team03.mopl.domain.entity.cqrs.PlaylistContentView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlaylistContentViewRepository extends JpaRepository<PlaylistContentView, PlaylistContentView.PlaylistContentId> {

    @Modifying
    @Query("UPDATE PlaylistContentView p SET p.title = :title, p.description = :description, " +
            "p.thumbnailKey = :thumbnailKey, p.tags = :tags, p.averageRating = :averageRating, " +
            "p.reviewCount = :reviewCount, p.watcherCount = :watcherCount " +
            "WHERE p.id.contentId = :contentId")
    void updateContentDetails(
            UUID contentId,
            String title,
            String description,
            String thumbnailKey,
            String tags,
            double averageRating,
            long reviewCount,
            long watcherCount
    );

    @Modifying
    @Query("DELETE FROM PlaylistContentView p WHERE p.id.contentId = :contentId")
    void deleteByContentId(UUID contentId);
}
