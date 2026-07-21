package org.codeit.sb06.team03.mopl.service.cqrs;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalFollowRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ExternalFollowQueryService {

    private final ExternalFollowRepository externalFollowRepository;

    public Set<UUID> getFollowerIds(UUID userId) {
        return externalFollowRepository.findFollowerIdsByFolloweeId(userId);
    }
}
