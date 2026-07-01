package org.codeit.sb06.team03.mopl.profile.application.out;

import org.codeit.sb06.team03.mopl.profile.ProfileReadModel;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface LoadProfilePort {

    Optional<Profile> load(UUID accountId);

    List<Profile> load(List<UUID> accountIds);

    Optional<ProfileReadModel> getProfileReadModel(UUID id);

    Map<UUID, ProfileReadModel> getProfileReadModels(List<UUID> ids);

    List<Profile> loadByNameContaining(String name);
}
