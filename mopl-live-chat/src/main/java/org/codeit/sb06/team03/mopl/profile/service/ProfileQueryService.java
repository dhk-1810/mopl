package org.codeit.sb06.team03.mopl.profile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.client.UserGrpcClient;
import org.codeit.sb06.team03.mopl.profile.domain.ProfileReadModel;
import org.codeit.sb06.team03.mopl.profile.domain.entity.ExternalProfileView;
import org.codeit.sb06.team03.mopl.profile.repository.ExternalProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ProfileQueryService {

    private final ExternalProfileRepository profileRepository;
    private final UserGrpcClient userGrpcClient;

    public ProfileReadModel getProfileReadModel(UUID id) {
        if (id == null) {
            return null;
        }
        return profileRepository.findById(id)
                .map(ProfileReadModel::from)
                .orElseGet(() -> fetchProfileViaGrpc(id));
    }

    public Map<UUID, ProfileReadModel> getProfileReadModels(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ExternalProfileView> list = profileRepository.findByAccountIdIn(ids);
        Map<UUID, ProfileReadModel> result = list.stream()
                .map(ProfileReadModel::from)
                .collect(Collectors.toMap(
                        ProfileReadModel::userId,
                        rm -> rm
                ));

        for (UUID id : ids) {
            if (!result.containsKey(id)) {
                ProfileReadModel fetched = fetchProfileViaGrpc(id);
                if (fetched != null) {
                    result.put(id, fetched);
                }
            }
        }
        return result;
    }

    public List<ExternalProfileView> loadByNameContaining(String name) {
        return profileRepository.findByNameContaining(name);
    }

    private ProfileReadModel fetchProfileViaGrpc(UUID userId) {
        try {
            UserGrpcClient.UserDto userDto = userGrpcClient.getUserById(userId);
            if (userDto != null) {
                return new ProfileReadModel(
                        userDto.id(),
                        userDto.name(),
                        userDto.profileImageUrl()
                );
            }
        } catch (Exception e) {
            log.error("Failed to fetch user profile via gRPC for userId: {}", userId, e);
        }
        return null;
    }
}
