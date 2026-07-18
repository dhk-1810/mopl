package org.codeit.sb06.team03.mopl.service.cqrs;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.follow.domain.Followee;
import org.codeit.sb06.team03.mopl.follow.service.FolloweeQueryService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ExternalFollowQueryServiceAdapter implements ExternalFollowQueryService {

    private final FolloweeQueryService followeeQueryService;

    @Override
    public List<UUID> getFollowerIds(UUID userId) {
        return followeeQueryService.findById(userId)
                .map(followee -> followee.getFollowers().stream()
                        .map(follower -> follower.getId().getFollowerId())
                        .toList())
                .orElse(Collections.emptyList());
    }
}
