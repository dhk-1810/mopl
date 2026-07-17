package org.codeit.sb06.team03.mopl.profile.repository;

import com.querydsl.core.types.Projections;
import io.github.openfeign.querydsl.jpa.spring.repository.QuerydslJpaRepository;
import org.codeit.sb06.team03.mopl.profile.domain.ProfileReadModel;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.codeit.sb06.team03.mopl.account.domain.QAccount.account;
import static org.codeit.sb06.team03.mopl.profile.domain.entity.QProfile.profile;

public interface ProfileRepository extends QuerydslJpaRepository<Profile, UUID> {

    List<Profile> findByAccountIdIn(Collection<UUID> accountIds);

    default List<Profile> findByNameContaining(String name) {
        if (name == null || name.isBlank()) {
            return List.of();
        }
        return select(profile)
                .from(profile)
                .where(profile.name.contains(name))
                .fetch();
    }

    default Optional<ProfileReadModel> findReadModelById(UUID id) {
        var result = select(Projections.constructor(ProfileReadModel.class,
                profile.accountId,
                profile.name,
                profile.imageKey,
                account.emailAddress.value,
                account.role.stringValue()
        ))
        .from(profile)
        .leftJoin(account).on(profile.accountId.eq(account.id))
        .where(profile.accountId.eq(id))
        .fetchOne();
        return Optional.ofNullable(result);
    }

    default List<ProfileReadModel> findReadModelsByIds(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return select(Projections.constructor(ProfileReadModel.class,
                profile.accountId,
                profile.name,
                profile.imageKey,
                account.emailAddress.value,
                account.role.stringValue()
        ))
        .from(profile)
        .leftJoin(account).on(profile.accountId.eq(account.id))
        .where(profile.accountId.in(ids))
        .fetch();
    }
}


