package org.codeit.sb06.team03.mopl.service.cqrs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.client.UserGrpcClient;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.repository.ExternalUserViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(value = "contentTransactionManager")
public class ExternalUserQueryService {

    private final ExternalUserViewRepository externalUserViewRepository;
    private final UserGrpcClient userGrpcClient;

    public ExternalUserView getProfile(UUID userId) {
        if (userId == null) {
            return null;
        }
        return externalUserViewRepository.findById(userId)
                .orElseGet(() -> fetchAndSaveProfile(userId));
    }

    public Map<UUID, ExternalUserView> getProfiles(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<UUID> distinctIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
        List<ExternalUserView> found = externalUserViewRepository.findAllById(distinctIds);
        Map<UUID, ExternalUserView> resultMap = new HashMap<>();
        for (ExternalUserView view : found) {
            resultMap.put(view.getId(), view);
        }

        for (UUID userId : distinctIds) {
            if (!resultMap.containsKey(userId)) {
                ExternalUserView view = fetchAndSaveProfile(userId);
                if (view != null) {
                    resultMap.put(userId, view);
                }
            }
        }
        return resultMap;
    }

    private ExternalUserView fetchAndSaveProfile(UUID userId) {
        UserGrpcClient.UserDto userDto = userGrpcClient.getUserById(userId);
        if (userDto == null) {
            log.warn("Failed to fetch user info via gRPC for userId: {}", userId);
            return null;
        }
        String imageKey = userDto.profileImageKey();
        if (imageKey != null && (imageKey.isBlank() || imageKey.startsWith("http://") || imageKey.startsWith("https://"))) {
            imageKey = null;
        }
        ExternalUserView view = ExternalUserView.create(userDto.id(), userDto.name(), imageKey);
        try {
            return externalUserViewRepository.save(view);
        } catch (Exception e) {
            log.error("Failed to save replicated ExternalUserView for userId: {}", userId, e);
            return view;
        }
    }
}
