package org.codeit.sb06.team03.mopl.user.domain;

import org.codeit.sb06.team03.mopl.user.domain.policy.ImageRegistrationPolicy;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Component
public class ProfileService {

    private final ImageRegistrationPolicy imageRegistrationPolicy;

    public ProfileService(ImageRegistrationPolicy profileImageRegistrationPolicy) {
        this.imageRegistrationPolicy = profileImageRegistrationPolicy;
    }

    public Profile create(UUID accountId, String name) {
        return Profile.create(accountId, name);
    }

    public Profile update(Profile profile, String name, @Nullable MultipartFile image) {
        return profile.update(name, image, imageRegistrationPolicy);
    }
}
