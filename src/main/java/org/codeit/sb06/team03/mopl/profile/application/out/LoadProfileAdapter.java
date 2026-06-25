package org.codeit.sb06.team03.mopl.profile.application.out;

import org.codeit.sb06.team03.mopl.profile.ProfileReadModel;
import org.codeit.sb06.team03.mopl.profile.domain.Profile;
import org.codeit.sb06.team03.mopl.profile.infra.out.ProfileRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class LoadProfileAdapter implements LoadProfilePort {

    private final ProfileRepository repository;

    public LoadProfileAdapter(ProfileRepository repository) {
        this.repository = repository;
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
    public Optional<ProfileReadModel> getProfileReadModel(UUID id) {
        return repository.findReadModelById(id);
    }

    @Override
    public Map<UUID, ProfileReadModel> getProfileReadModels(List<UUID> ids) {
        List<ProfileReadModel> list = repository.findReadModelsByIds(ids);
        return list.stream()
                .collect(Collectors.toMap(
                        ProfileReadModel::userId,
                        rm -> rm,
                        (existing, replacement) -> existing
                ));
    }

}
