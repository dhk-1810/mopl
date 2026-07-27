package org.codeit.sb06.team03.mopl.service.composite;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dto.UserSummary;
import org.codeit.sb06.team03.mopl.dto.response.WatchingSessionDto;
import org.codeit.sb06.team03.mopl.enums.SortDirection;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponseWatchingSessionDto;
import org.codeit.sb06.team03.mopl.dto.request.CursorWatchingSessionRequest;
import org.codeit.sb06.team03.mopl.image.service.ExternalImageQueryService;
import org.codeit.sb06.team03.mopl.profile.domain.entity.ExternalProfileView;
import org.codeit.sb06.team03.mopl.profile.domain.ProfileReadModel;
import org.codeit.sb06.team03.mopl.profile.service.ProfileQueryService;
import org.codeit.sb06.team03.mopl.dto.WatchingSessionReadModel;
import org.codeit.sb06.team03.mopl.service.application.WatchingSessionQueryService;
import org.springframework.data.domain.Slice;
import org.codeit.sb06.team03.mopl.exception.WatchingSessionAccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class WatchingSessionCompositeService {

    private final WatchingSessionQueryService watchingSessionQueryService;
    private final ProfileQueryService profileQueryService;
    private final ExternalImageQueryService imageQueryService;

    public WatchingSessionDto getByWatcherId(UUID watcherId, String authenticatedUserId) {

        // TODO 자발/강제 로그아웃 시 워칭세션 삭제
        WatchingSessionReadModel watchingSession = watchingSessionQueryService.getByContentId(watcherId);
        if (watchingSession == null) return null;

        ProfileReadModel profile = profileQueryService.getProfileReadModels(List.of(watcherId)).get(watcherId);
        String url = (profile != null) ? imageQueryService.getPresignedUrl(profile.imageKey()) : null;
        String name = (profile != null) ? profile.name() : "Unknown";

        UserSummary watcher = new UserSummary(watcherId, name, url);

        return new WatchingSessionDto(
                watchingSession.id(),
                watchingSession.createdAt(),
                watcher
        );
    }

    public CursorResponseWatchingSessionDto getByContentId(UUID contentId, CursorWatchingSessionRequest request) {

        List<UUID> filteredWatcherIds = null;
        if (request.watcherNameLike() != null && !request.watcherNameLike().isBlank()) {
            filteredWatcherIds = profileQueryService.loadByNameContaining(request.watcherNameLike())
                    .stream()
                    .map(ExternalProfileView::getAccountId)
                    .toList();
        }

        Slice<WatchingSessionReadModel> slice = watchingSessionQueryService.getByContentId(contentId, filteredWatcherIds, request);
        List<WatchingSessionReadModel> watchingSessions = slice.getContent();

        List<UUID> watcherIds = watchingSessions.stream().map(WatchingSessionReadModel::watcherId).toList();
        Map<UUID, ProfileReadModel> profilesMap = profileQueryService.getProfileReadModels(watcherIds);
        Map<UUID, UserSummary> watchers = profilesMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            ProfileReadModel profile = entry.getValue();
                            String url = imageQueryService.getPresignedUrl(profile.imageKey());
                            return new UserSummary(profile.userId(), profile.name(), url);
                        }
                ));


        List<WatchingSessionDto> response = watchingSessions.stream()
                .map(rm -> new WatchingSessionDto(
                        rm.id(),
                        rm.createdAt(),
                        watchers.get(rm.watcherId())
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
