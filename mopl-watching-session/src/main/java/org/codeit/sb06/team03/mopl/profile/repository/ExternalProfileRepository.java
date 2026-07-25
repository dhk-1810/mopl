package org.codeit.sb06.team03.mopl.profile.repository;

import org.codeit.sb06.team03.mopl.profile.domain.entity.ExternalProfileView;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ExternalProfileRepository {
    public Optional<ExternalProfileView> findById(UUID id) {
        return Optional.empty();
    }

    public List<ExternalProfileView> findByAccountIdIn(List<UUID> accountIds) {
        return Collections.emptyList();
    }

    public List<ExternalProfileView> findByNameContaining(String name) {
        return Collections.emptyList();
    }
}
