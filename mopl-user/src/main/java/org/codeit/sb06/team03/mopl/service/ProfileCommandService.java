package org.codeit.sb06.team03.mopl.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.Profile;
import org.codeit.sb06.team03.mopl.exception.profile.ProfileNotFoundException;
import org.codeit.sb06.team03.mopl.repository.ProfileRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class ProfileCommandService {

    private final ProfileRepository profileRepository;
    private final ImageCommandService imageCommandService;

    public Profile create(UUID accountId, String name) {
        return Profile.create(accountId, name);
    }

    public Profile update(UUID accountId, String name, @Nullable MultipartFile image) {
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

