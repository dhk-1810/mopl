package org.codeit.sb06.team03.mopl.repository;

import org.codeit.sb06.team03.mopl.entity.InboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface InboxEventRepository extends JpaRepository<InboxEvent, UUID> {

    boolean existsByMessageId(String messageId);

    @Modifying
    @Query("DELETE FROM InboxEvent i WHERE i.processedAt < :threshold")
    int deleteByProcessedAtBefore(@Param("threshold") Instant threshold);
}
