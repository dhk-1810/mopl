package org.codeit.sb06.team03.mopl.infra.out.cqrs;

import org.codeit.sb06.team03.mopl.domain.entity.cqrs.PlaylistView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlaylistViewRepository extends JpaRepository<PlaylistView, UUID> {

    @Modifying
    @Query("UPDATE PlaylistView p SET p.ownerName = :ownerName, p.ownerProfileImageKey = :ownerProfileImageKey WHERE p.ownerId = :ownerId")
    void updateOwnerDetails(UUID ownerId, String ownerName, String ownerProfileImageKey);
}
