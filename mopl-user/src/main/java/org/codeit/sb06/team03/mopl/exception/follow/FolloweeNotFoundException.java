package org.codeit.sb06.team03.mopl.exception.follow;

import java.util.UUID;

public class FolloweeNotFoundException extends FollowException {

    public FolloweeNotFoundException(UUID followeeId) {
        super("Followee not found: " + followeeId);
    }
}
