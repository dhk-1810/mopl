package org.codeit.sb06.team03.mopl.service.cqrs;

import java.util.List;
import java.util.UUID;

public interface ExternalFollowQueryService {
    List<UUID> getFollowerIds(UUID userId);
}
