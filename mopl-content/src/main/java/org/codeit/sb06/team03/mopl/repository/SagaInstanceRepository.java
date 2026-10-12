package org.codeit.sb06.team03.mopl.repository;

import org.codeit.sb06.team03.mopl.entity.SagaInstance;
import org.codeit.sb06.team03.mopl.enums.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SagaInstanceRepository extends JpaRepository<SagaInstance, UUID> {

    List<SagaInstance> findAllByStatusAndCreatedAtBefore(SagaStatus status, Instant threshold);
}
