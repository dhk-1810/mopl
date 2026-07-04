package org.codeit.sb06.team03.mopl.profile.application.in;

import org.codeit.sb06.team03.mopl.profile.ProfileReadModel;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface GetProfileUseCase {

    Profile getById(UUID accountId);

    List<Profile> getByIdsIn(List<UUID> accountIds);

    ProfileReadModel getProfileReadModel(UUID id);

    Map<UUID, ProfileReadModel> getProfileReadModels(List<UUID> ids);

    List<Profile> loadByNameContaining(String name);

}
