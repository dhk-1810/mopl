package org.codeit.sb06.team03.mopl.repository;

import org.codeit.sb06.team03.mopl.entity.OutboxEvent;
import org.codeit.sb06.team03.mopl.enums.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
            OutboxStatus status,
            int maxRetries,
            Pageable pageable
    );

    @Modifying
    @Query("DELETE FROM OutboxEvent o WHERE o.status = :status AND o.createdAt < :threshold")
    int deleteByStatusAndCreatedAtBefore(
            @Param("status") OutboxStatus status,
            @Param("threshold") Instant threshold
    );
}
