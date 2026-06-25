package org.codeit.sb06.team03.mopl.profile.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.profile.ProfileReadModel;
import org.codeit.sb06.team03.mopl.profile.application.in.GetProfileUseCase;
import org.codeit.sb06.team03.mopl.profile.application.out.LoadProfilePort;
import org.codeit.sb06.team03.mopl.profile.domain.Profile;
import org.codeit.sb06.team03.mopl.profile.domain.exception.ProfileNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ProfileQueryService implements GetProfileUseCase {

    private final LoadProfilePort loadProfilePort;

    @Override
    public Profile load(UUID accountId) {
        return loadProfilePort.load(accountId)
                .orElseThrow(() -> new ProfileNotFoundException(accountId));
    }

    @Override
    public List<Profile> load(List<UUID> accountIds) {
        return loadProfilePort.load(accountIds);
    }

    @Override
    public ProfileReadModel getProfileReadModel(UUID id) {
        return loadProfilePort.getProfileReadModel(id)
                .orElseThrow(() -> new ProfileNotFoundException(id));
    }

    @Override
    public Map<UUID, ProfileReadModel> getProfileReadModels(List<UUID> ids) {
        return loadProfilePort.getProfileReadModels(ids);
    }

}
