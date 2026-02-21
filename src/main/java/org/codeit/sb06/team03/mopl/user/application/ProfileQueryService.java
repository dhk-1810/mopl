package org.codeit.sb06.team03.mopl.user.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummaryDto;
import org.codeit.sb06.team03.mopl.user.application.in.GetProfileUseCase;
import org.codeit.sb06.team03.mopl.user.application.out.LoadProfilePort;
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
    public Optional<UserSummaryDto> getUserSummary(UUID id) {
        return loadProfilePort.getUserSummary(id);
    }

    @Override
    public Map<UUID, UserSummaryDto> getUserSummaries(List<UUID> ids) {
        return loadProfilePort.getUserSummaries(ids);
    }
}
