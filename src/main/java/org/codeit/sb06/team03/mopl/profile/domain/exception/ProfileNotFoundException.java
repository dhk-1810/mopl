package org.codeit.sb06.team03.mopl.profile.domain.exception;

import java.util.UUID;

public class ProfileNotFoundException extends UserException {
    
    public ProfileNotFoundException(UUID accountId) {
        super("Profile not found for account: " + accountId);
    }
}
