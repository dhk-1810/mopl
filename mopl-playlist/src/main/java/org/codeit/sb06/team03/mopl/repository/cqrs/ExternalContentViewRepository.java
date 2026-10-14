package org.codeit.sb06.team03.mopl.repository.cqrs;

import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalContentView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface ExternalContentViewRepository extends JpaRepository<ExternalContentView, UUID> {

    @Modifying
    @Query("UPDATE ExternalContentView v SET v.deletedAt = NULL WHERE v.id = :id")
    void restoreById(@Param("id") UUID id);

    @Modifying
    @Query("DELETE FROM ExternalContentView v WHERE v.deletedAt IS NOT NULL AND v.deletedAt < :threshold")
    int purgeDeletedBefore(@Param("threshold") Instant threshold);
}
