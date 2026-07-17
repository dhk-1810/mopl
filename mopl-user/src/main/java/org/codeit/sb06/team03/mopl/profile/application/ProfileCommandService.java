package org.codeit.sb06.team03.mopl.profile.application;

import org.codeit.sb06.team03.mopl.image.application.ImageCommandService;
import org.codeit.sb06.team03.mopl.profile.application.in.CreateProfileCommand;
import org.codeit.sb06.team03.mopl.profile.application.in.UpdateProfileCommand;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;
import org.codeit.sb06.team03.mopl.profile.domain.ProfileService;
import org.codeit.sb06.team03.mopl.profile.domain.exception.ProfileNotFoundException;
import org.codeit.sb06.team03.mopl.profile.infra.out.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Transactional
public class ProfileCommandService {

    private final ProfileService profileService;
    private final ProfileRepository profileRepository;
    private final ImageCommandService imageCommandService;

    public ProfileCommandService(
            ProfileService profileService,
            ProfileRepository profileRepository,
            ImageCommandService imageCommandService
    ) {
        this.profileService = profileService;
        this.profileRepository = profileRepository;
        this.imageCommandService = imageCommandService;
    }

    public Profile create(CreateProfileCommand command) {
        final UUID accountId = command.accountId();
        final String name = command.name();

        return profileService.create(accountId, name);
    }

    public Profile update(UpdateProfileCommand command) {
        final UUID accountId = command.accountId();
        final String name = command.name();
        final MultipartFile image = command.image();

        Profile profile = profileRepository.findById(accountId)
                .orElseThrow(() -> new ProfileNotFoundException(accountId));

        String imageKey = null;
        if (image != null && !image.isEmpty()) {
            imageKey = imageCommandService.register(image);
        }

        Profile updated = profileService.update(profile, name, imageKey);
        return profileRepository.save(updated);
    }
}

