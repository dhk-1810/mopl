package org.codeit.sb06.team03.mopl.notification.infra.out.cqrs;

import org.codeit.sb06.team03.mopl.notification.domain.entity.cqrs.ExternalUserView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExternalUserViewRepository extends JpaRepository<ExternalUserView, UUID> {
}
