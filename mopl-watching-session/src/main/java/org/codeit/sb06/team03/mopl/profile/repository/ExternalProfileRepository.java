package org.codeit.sb06.team03.mopl.profile.repository;

import org.codeit.sb06.team03.mopl.profile.domain.entity.ExternalProfileView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExternalProfileRepository extends JpaRepository<ExternalProfileView, UUID> {
    List<ExternalProfileView> findByAccountIdIn(List<UUID> accountIds);
    List<ExternalProfileView> findByNameContaining(String name);
}
