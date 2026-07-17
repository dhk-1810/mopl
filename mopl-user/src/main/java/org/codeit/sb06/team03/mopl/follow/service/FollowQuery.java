package org.codeit.sb06.team03.mopl.follow.service;

import java.util.UUID;

public record FollowQuery(UUID followeeId, UUID followerId) {
}
