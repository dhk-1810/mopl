package org.codeit.sb06.team03.mopl.service.cqrs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.client.UserGrpcClient;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalUserViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(value = "playlistTransactionManager")
public class ExternalUserQueryService {

    private final ExternalUserViewRepository externalUserViewRepository;
    private final UserGrpcClient userGrpcClient;
    private final ExternalUserWriter externalUserWriter;

    @Transactional(value = "playlistTransactionManager", readOnly = true)
    public Map<UUID, ExternalUserView> getProfiles(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<UUID, ExternalUserView> existing = externalUserViewRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(ExternalUserView::getId, Function.identity()));

        Map<UUID, ExternalUserView> result = new HashMap<>(existing);
        for (UUID userId : userIds) {
            if (!result.containsKey(userId)) {
                ExternalUserView fetched = fetchAndSaveProfile(userId);
                if (fetched != null) {
                    result.put(userId, fetched);
                }
            }
        }
        return result;
    }

    public ExternalUserView getProfile(UUID userId) {
        if (userId == null) {
            return null;
        }
        return externalUserViewRepository.findById(userId)
                .orElseGet(() -> fetchAndSaveProfile(userId));
    }

    private ExternalUserView fetchAndSaveProfile(UUID userId) {
        UserGrpcClient.UserDto userDto = userGrpcClient.getUserById(userId);
        if (userDto == null) {
            log.warn("Failed to fetch user info via gRPC for userId: {}", userId);
            return null;
        }
        try {
            return externalUserWriter.saveFromDto(userDto);
        } catch (Exception e) {
            log.error("Failed to save replicated ExternalUserView for userId: {}", userId, e);
            return ExternalUserView.create(userDto.id(), userDto.name(), userDto.profileImageUrl());
        }
    }
}
