package org.codeit.sb06.team03.mopl.repository.cqrs;

import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalFollowView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ExternalFollowRepository extends JpaRepository<ExternalFollowView, ExternalFollowView.ExternalFollowId> {
    @Query("SELECT f.id.followerId FROM ExternalFollowView f WHERE f.id.followeeId = :followeeId")
    Set<UUID> findFollowerIdsByFolloweeId(@Param("followeeId") UUID followeeId);
}
