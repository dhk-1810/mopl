package org.codeit.sb06.team03.mopl.composite;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.WatchingSessionDto;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.common.security.MoplUserDetails;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.application.in.CursorResponseWatchingSessionDto;
import org.codeit.sb06.team03.mopl.content.application.in.GetContentUseCase;
import org.codeit.sb06.team03.mopl.content.infra.in.CursorWatchingSessionRequest;
import org.codeit.sb06.team03.mopl.image.application.in.GetPresignedUrlUseCase;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummaryDto;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;
import org.codeit.sb06.team03.mopl.profile.ProfileReadModel;
import org.codeit.sb06.team03.mopl.profile.application.in.GetProfileUseCase;
import org.codeit.sb06.team03.mopl.watchingSession.WatchingSessionReadModel;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.GetWatchingSessionUseCase;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class WatchingSessionCompositeService {

    private final GetWatchingSessionUseCase getWatchingSessionUseCase;
    private final GetContentUseCase getSingleContentUseCase;
    private final GetProfileUseCase getProfileUseCase;
    private final GetPresignedUrlUseCase getPresignedUrlUseCase;

    public WatchingSessionDto getWatchingSession(UUID watcherId, MoplUserDetails userDetails) {

        // TODO 자발/강제 로그아웃 시 워칭세션 삭제
        WatchingSessionReadModel watchingSession = getWatchingSessionUseCase.get(watcherId);
        ContentReadModel content = getSingleContentUseCase.get(watchingSession.liveChatId());
        var userDto = userDetails.getUserDto();
        UserSummaryDto watcher = new UserSummaryDto(userDto.id(), userDto.name(), userDto.profilePresignedUrl());

        return new WatchingSessionDto(
                watchingSession.id(),
                watchingSession.createdAt(),
                watcher,
                content
        );
    }

    public CursorResponseWatchingSessionDto getWatchingSessions(UUID contentId, CursorWatchingSessionRequest request) {

        List<UUID> filteredWatcherIds = null;
        if (request.watcherNameLike() != null && !request.watcherNameLike().isBlank()) {
            filteredWatcherIds = getProfileUseCase.loadByNameContaining(request.watcherNameLike())
                    .stream()
                    .map(Profile::getAccountId)
                    .toList();
        }

        Slice<WatchingSessionReadModel> slice = getWatchingSessionUseCase.get(contentId, filteredWatcherIds, request);
        List<WatchingSessionReadModel> watchingSessions = slice.getContent();

        List<UUID> watcherIds = watchingSessions.stream().map(WatchingSessionReadModel::watcherId).toList();
        Map<UUID, ProfileReadModel> profilesMap = getProfileUseCase.getProfileReadModels(watcherIds);
        Map<UUID, UserSummaryDto> watchers = profilesMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            ProfileReadModel profile = entry.getValue();
                            String url = getPresignedUrlUseCase.getPresignedUrl(profile.imageKey());
                            return new UserSummaryDto(profile.userId(), profile.name(), url);
                        }
                ));

        ContentReadModel content = getSingleContentUseCase.get(contentId);

        List<WatchingSessionDto> response = watchingSessions.stream()
                .map(rm -> new WatchingSessionDto(
                        rm.id(),
                        rm.createdAt(),
                        watchers.get(rm.watcherId()),
                        content
                ))
                .toList();

        String nextCursor = null;
        UUID nextIdAfter = null;
        if (slice.hasNext()) {
            WatchingSessionDto lastItem = response.getLast();
            nextCursor = lastItem.createdAt().toString();
            nextIdAfter = lastItem.id();
        }

        return new CursorResponseWatchingSessionDto(
                response,
                nextCursor,
                nextIdAfter,
                slice.hasNext(),
                0,
                SortDirection.valueOf(request.sortDirection()),
                request.sortBy()
        );
    }
}



