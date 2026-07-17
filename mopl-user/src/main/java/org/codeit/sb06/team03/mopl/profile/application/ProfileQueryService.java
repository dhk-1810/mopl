package org.codeit.sb06.team03.mopl.profile.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.profile.ProfileReadModel;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;
import org.codeit.sb06.team03.mopl.profile.domain.exception.ProfileNotFoundException;
import org.codeit.sb06.team03.mopl.profile.infra.out.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProfileQueryService {

    private final ProfileRepository profileRepository;

    public Profile getById(UUID accountId) {
        return profileRepository.findById(accountId)
                .orElseThrow(() -> new ProfileNotFoundException(accountId));
    }

    public List<Profile> getByIdsIn(List<UUID> accountIds) {
        return profileRepository.findByAccountIdIn(accountIds);
    }

    public ProfileReadModel getProfileReadModel(UUID id) {
        return profileRepository.findReadModelById(id)
                .orElseThrow(() -> new ProfileNotFoundException(id));
    }

    public Map<UUID, ProfileReadModel> getProfileReadModels(List<UUID> ids) {
        List<ProfileReadModel> list = profileRepository.findReadModelsByIds(ids);
        return list.stream()
                .collect(Collectors.toMap(
                        ProfileReadModel::userId,
                        rm -> rm,
                        (existing, replacement) -> existing
                ));
    }

    public List<Profile> loadByNameContaining(String name) {
        return profileRepository.findByNameContaining(name);
    }

}
