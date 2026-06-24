package org.codeit.sb06.team03.mopl.profile.infra.out;

import io.github.openfeign.querydsl.jpa.spring.repository.QuerydslJpaRepository;
import org.codeit.sb06.team03.mopl.profile.domain.Profile;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProfileRepository extends QuerydslJpaRepository<Profile, UUID> {

    List<Profile> findByAccountIdIn(Collection<UUID> accountIds);

}

