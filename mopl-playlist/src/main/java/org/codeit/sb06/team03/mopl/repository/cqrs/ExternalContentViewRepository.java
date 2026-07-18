package org.codeit.sb06.team03.mopl.repository.cqrs;

import org.codeit.sb06.team03.mopl.domain.entity.cqrs.ExternalContentView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExternalContentViewRepository extends JpaRepository<ExternalContentView, UUID> {
}
