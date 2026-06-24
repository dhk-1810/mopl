package org.codeit.sb06.team03.mopl.profile.application.out;

import org.codeit.sb06.team03.mopl.image.application.in.GetPresignedUrlUseCase;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummaryDto;
import org.codeit.sb06.team03.mopl.profile.domain.Profile;
import org.codeit.sb06.team03.mopl.profile.infra.out.ProfileRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LoadProfileAdapter implements LoadProfilePort {

    private final ProfileRepository repository;
    private final GetPresignedUrlUseCase getPresignedUrlUseCase;

    public LoadProfileAdapter(ProfileRepository repository, GetPresignedUrlUseCase getPresignedUrlUseCase) {
        this.repository = repository;
        this.getPresignedUrlUseCase = getPresignedUrlUseCase;
    }

    @Override
    public Optional<Profile> load(UUID accountId) {
        return repository.findById(accountId);
    }

    @Override
    public List<Profile> load(List<UUID> accountIds) {
        return repository.findByAccountIdIn(accountIds);
    }

    @Override
    public Optional<UserSummaryDto> getUserSummary(UUID id) {
        return repository.findById(id)
                .map(profile -> {
                    String url = getPresignedUrlUseCase.getPresignedUrl(profile.getImageKey());
                    return new UserSummaryDto(profile.getAccountId(), profile.getName(), url);
                });
    }

    @Override
    public Map<UUID, UserSummaryDto> getUserSummaries(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Profile> profiles = repository.findByAccountIdIn(ids);
        Map<UUID, UserSummaryDto> result = new HashMap<>();
        for (Profile profile : profiles) {
            String url = getPresignedUrlUseCase.getPresignedUrl(profile.getImageKey());
            result.put(profile.getAccountId(), new UserSummaryDto(profile.getAccountId(), profile.getName(), url));
        }
        return result;
    }
}

