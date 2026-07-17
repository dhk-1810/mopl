package org.codeit.sb06.team03.mopl.follow.controller;

import org.codeit.sb06.team03.mopl.follow.service.CreateFollowCommand;
import org.codeit.sb06.team03.mopl.follow.service.FollowCommand;
import org.codeit.sb06.team03.mopl.follow.service.FollowQuery;
import org.codeit.sb06.team03.mopl.follow.service.UnfollowCommand;
import org.codeit.sb06.team03.mopl.follow.controller.FollowRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FollowMapper {

    public CreateFollowCommand toCommand(UUID accountId) {
        return new CreateFollowCommand(accountId);
    }

    public FollowCommand toCommand(FollowRequest request, UUID userId) {
        UUID followeeId = UUID.fromString(request.followeeId());
        UUID followerId = userId;
        return new FollowCommand(followeeId, followerId);
    }

    public UnfollowCommand toCommand(String followId, UUID userId) {
        UUID followeeId = userId;
        UUID unfollowId = UUID.fromString(followId);
        return new UnfollowCommand(followeeId, unfollowId);
    }

    public FollowQuery toQuery(String followeeId, UUID userId) {
        UUID followeeUUID = UUID.fromString(followeeId);
        UUID followerId = userId;
        return new FollowQuery(followeeUUID, followerId);
    }
}
