package org.codeit.sb06.team03.mopl.profile.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;
import org.codeit.sb06.team03.mopl.profile.exception.ProfileNotFoundException;
import org.codeit.sb06.team03.mopl.profile.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class ProfileCommandService {

    private final ProfileRepository profileRepository;
    private final ImageCommandService imageCommandService;

    public Profile create(CreateProfileCommand command) {
        final UUID accountId = command.accountId();
        final String name = command.name();

        return Profile.create(accountId, name);
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

        Profile updated = profile.update(name, imageKey);
        return profileRepository.save(updated);
    }
}

