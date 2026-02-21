package org.codeit.sb06.team03.mopl.user.application.in;

import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummaryDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface GetProfileUseCase {

    Optional<UserSummaryDto> getUserSummary(UUID id);

    Map<UUID, UserSummaryDto> getUserSummaries(List<UUID> ids);

}
