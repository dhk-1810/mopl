package org.codeit.sb06.team03.mopl.profile.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummaryDto;
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
    public UserSummaryDto getUserSummary(UUID id) {
        return loadProfilePort.getUserSummary(id)
                .orElseThrow(() -> new ProfileNotFoundException(id));
    }

    @Override
    public Map<UUID, UserSummaryDto> getUserSummaries(List<UUID> ids) {
        return loadProfilePort.getUserSummaries(ids);
    }

    @Override
    public Profile getDMUserProfile(UUID userId) {
        return loadProfilePort.load(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));
    }
}
