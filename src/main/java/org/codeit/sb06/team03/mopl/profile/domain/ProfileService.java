package org.codeit.sb06.team03.mopl.profile.domain;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProfileService {

    public Profile create(UUID accountId, String name) {
        return Profile.create(accountId, name);
    }

    public Profile update(Profile profile, String name, @Nullable String imageKey) {
        return profile.update(name, imageKey);
    }
}

