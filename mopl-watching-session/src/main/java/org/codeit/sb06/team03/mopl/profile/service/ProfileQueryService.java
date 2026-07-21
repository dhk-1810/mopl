package org.codeit.sb06.team03.mopl.profile.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.profile.domain.ProfileReadModel;
import org.codeit.sb06.team03.mopl.profile.domain.entity.ExternalProfileView;
import org.codeit.sb06.team03.mopl.profile.repository.ExternalProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ProfileQueryService {

    private final ExternalProfileRepository profileRepository;

    public ProfileReadModel getProfileReadModel(UUID id) {
        return profileRepository.findById(id)
                .map(ProfileReadModel::from)
                .orElse(null);
    }

    public Map<UUID, ProfileReadModel> getProfileReadModels(List<UUID> ids) {
        List<ExternalProfileView> list = profileRepository.findByAccountIdIn(ids);
        return list.stream()
                .map(ProfileReadModel::from)
                .collect(Collectors.toMap(
                        ProfileReadModel::userId,
                        rm -> rm
                ));
    }

    public List<ExternalProfileView> loadByNameContaining(String name) {
        return profileRepository.findByNameContaining(name);
    }
}
