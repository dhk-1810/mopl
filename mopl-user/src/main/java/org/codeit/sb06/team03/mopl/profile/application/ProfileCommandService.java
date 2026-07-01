package org.codeit.sb06.team03.mopl.profile.application;

import org.codeit.sb06.team03.mopl.image.application.in.RegisterImageUseCase;
import org.codeit.sb06.team03.mopl.profile.application.in.CreateProfileCommand;
import org.codeit.sb06.team03.mopl.profile.application.in.CreateProfileUseCase;
import org.codeit.sb06.team03.mopl.profile.application.in.UpdateProfileCommand;
import org.codeit.sb06.team03.mopl.profile.application.in.UpdateProfileUseCase;
import org.codeit.sb06.team03.mopl.profile.application.out.LoadProfilePort;
import org.codeit.sb06.team03.mopl.profile.application.out.SaveProfilePort;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;
import org.codeit.sb06.team03.mopl.profile.domain.ProfileService;
import org.codeit.sb06.team03.mopl.profile.domain.exception.ProfileNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Transactional
public class ProfileCommandService implements CreateProfileUseCase, UpdateProfileUseCase {

    private final ProfileService profileService;
    private final SaveProfilePort saveProfilePort;
    private final LoadProfilePort loadProfilePort;
    private final RegisterImageUseCase registerImageUseCase;

    public ProfileCommandService(
            ProfileService profileService,
            SaveProfilePort saveProfilePort,
            LoadProfilePort loadProfilePort,
            RegisterImageUseCase registerImageUseCase
    ) {
        this.profileService = profileService;
        this.saveProfilePort = saveProfilePort;
        this.loadProfilePort = loadProfilePort;
        this.registerImageUseCase = registerImageUseCase;
    }

    @Override
    public Profile create(CreateProfileCommand command) {
        final UUID accountId = command.accountId();
        final String name = command.name();

        return profileService.create(accountId, name);
    }

    @Override
    public Profile update(UpdateProfileCommand command) {
        final UUID accountId = command.accountId();
        final String name = command.name();
        final MultipartFile image = command.image();

        Profile profile = loadProfilePort.load(accountId)
                .orElseThrow(() -> new ProfileNotFoundException(accountId));

        String imageKey = null;
        if (image != null && !image.isEmpty()) {
            imageKey = registerImageUseCase.register(image);
        }

        Profile updated = profileService.update(profile, name, imageKey);
        return saveProfilePort.save(updated);
    }
}

