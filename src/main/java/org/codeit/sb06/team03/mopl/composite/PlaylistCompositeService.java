package org.codeit.sb06.team03.mopl.composite;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.common.security.MoplUserDetails;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.application.in.GetContentUseCase;
import org.codeit.sb06.team03.mopl.content.infra.ContentDto;
import org.codeit.sb06.team03.mopl.image.application.in.GetPresignedUrlUseCase;
import org.codeit.sb06.team03.mopl.playlist.PlaylistReadModel;
import org.codeit.sb06.team03.mopl.playlist.application.in.*;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.Playlist;
import org.codeit.sb06.team03.mopl.playlist.infra.in.*;
import org.codeit.sb06.team03.mopl.playlist.infra.in.request.CursorRequestPlaylistDto;
import org.codeit.sb06.team03.mopl.playlist.infra.in.request.PlaylistCreateRequest;
import org.codeit.sb06.team03.mopl.playlist.infra.in.request.PlaylistUpdateRequest;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.CursorResponsePlaylistDto;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.PlaylistDto;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummaryDto;
import org.codeit.sb06.team03.mopl.profile.application.in.GetProfileUseCase;
import org.codeit.sb06.team03.mopl.profile.domain.Profile;
import org.codeit.sb06.team03.mopl.profile.domain.exception.ProfileNotFoundException;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.GetWatchingSessionUseCase;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PlaylistCompositeService {

    private final GetPresignedUrlUseCase getPresignedUrlUseCase;

    private final PlaylistMapper playlistMapper;
    private final CreatePlaylistUseCase createPlaylistUseCase;
    private final GetPlaylistsUseCase getPlaylistsUseCase;
    private final GetSinglePlaylistUseCase getPlaylistUseCase;
    private final UpdatePlaylistUseCase updatePlaylistUseCase;
    private final DeletePlaylistUseCase deletePlaylistUseCase;

    private final AddContentToCurationUseCase addContentToCurationUseCase;
    private final DeleteContentFromCurationUseCase deleteContentFromCurationUseCase;

    private final SubscribePlaylistUseCase subscribePlaylistUseCase;
    private final UnsubscribePlaylistUseCase unsubscribePlaylistUseCase;

    private final GetProfileUseCase getProfileUseCase;
    private final GetSubscriptionUseCase getSubscriptionUseCase;

    private final GetContentUseCase getContentUseCase;
    private final GetCurationUseCase getCurationUseCase;

    private final GetWatchingSessionUseCase getWatchingSessionUseCase;

    public PlaylistDto createPlaylist(PlaylistCreateRequest request, UUID ownerId) {

        CreatePlaylistCommand command = playlistMapper.toCommand(request);
        Playlist playlist = createPlaylistUseCase.create(command, ownerId);

        MoplUserDetails userDetails = (MoplUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        UserSummaryDto owner = new UserSummaryDto(
                userDetails.getId(),
                userDetails.getUserDto().name(),
                userDetails.getUserDto().profilePresignedUrl());

        return PlaylistDto.toDto(playlist, owner, false , Collections.emptyList());
    }

    public CursorResponsePlaylistDto getAll(CursorRequestPlaylistDto request, UUID viewerId) {
        Slice<PlaylistReadModel> slice = getPlaylistsUseCase.get(request, viewerId);
        List<PlaylistReadModel> readModels = slice.getContent();

        List<UUID> ownerIds = readModels.stream().map(PlaylistReadModel::ownerId).toList();
        List<Profile> profileList = getProfileUseCase.load(ownerIds);
        Map<UUID, UserSummaryDto> owners = profileList.stream()
                .collect(Collectors.toMap(
                        Profile::getAccountId,
                        profile -> UserSummaryDto.from(profile, getPresignedUrlUseCase),
                        (existing, replacement) -> existing // 중복 ID 발생 시 기존 값 유지
                ));

        Set<UUID> playlistIds = readModels.stream().map(PlaylistReadModel::id).collect(Collectors.toSet());
        Map<UUID, Boolean> subscribedByMe = getSubscriptionUseCase.isSubscribed(playlistIds, viewerId);

        // 1. 플레이리스트별 컨텐츠 ID 가져옴
        Map<UUID, List<UUID>> contentIdsMap = getCurationUseCase.getContentIdsByPlaylistIds(playlistIds);

        // 2. 컨텐츠 전체를 한번에 조회 및 presigned URL 일괄 생성
        Set<UUID> allContentIds = contentIdsMap.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        List<ContentReadModel> contentReadModels = allContentIds.isEmpty()
                ? Collections.emptyList()
                : getContentUseCase.getByIds(allContentIds);

        List<String> thumbnailKeys = contentReadModels.stream()
                .map(ContentReadModel::thumbnailKey)
                .filter(Objects::nonNull)
                .toList();
        Map<String, String> urls = getPresignedUrlUseCase.getPresignedUrls(thumbnailKeys);

        List<ContentDto> contentDtos = contentReadModels.stream()
                .map(rm -> ContentDto.from(rm, urls.get(rm.thumbnailKey())))
                .toList();

        // 3. 플레이리스트별로 골라 담음
        Map<UUID, List<ContentDto>> contentsMap = playlistIds.stream()
                .collect(Collectors.toMap(
                        playlistId -> playlistId,
                        playlistId -> {
                            List<UUID> contentIds = contentIdsMap.getOrDefault(playlistId, Collections.emptyList());
                            return contentDtos.stream()
                                    .filter(dto -> contentIds.contains(dto.id()))
                                    .toList();
                        }
                ));

        List<PlaylistDto> data = readModels.stream()
                .map(readModel -> PlaylistDto.toDto(
                        readModel,
                        owners.get(readModel.ownerId()),
                        subscribedByMe.getOrDefault(readModel.id(), false),
                        contentsMap.getOrDefault(readModel.id(), Collections.emptyList())
                ))
                .toList();

        String nextCursor = null;
        UUID nextIdAfter = null;
        if (slice.hasNext()) {
            PlaylistReadModel lastItem = readModels.getLast();
            nextCursor = lastItem.updatedAt().toString();
            nextIdAfter = lastItem.id();
        }

        return new CursorResponsePlaylistDto(
                data,
                nextCursor,
                nextIdAfter,
                slice.hasNext(),
                0,
                request.sortBy(),
                SortDirection.valueOf(request.sortDirection())
        );
    }

    public PlaylistDto get(String playlistId, UUID viewerId) {
        PlaylistReadModel readModel = getPlaylistUseCase.get(playlistId, viewerId);

        Profile profile = getProfileUseCase.load(readModel.ownerId())
                .orElseThrow(() -> new ProfileNotFoundException(readModel.ownerId()));
        UserSummaryDto owner = UserSummaryDto.from(profile, getPresignedUrlUseCase);

        boolean subscribedByMe = getSubscriptionUseCase.isSubscribed(playlistId, viewerId);
        List<ContentDto> contentDtos = getContentDtos(readModel.id());
        return PlaylistDto.toDto(readModel, owner, subscribedByMe, contentDtos);
    }

    public PlaylistDto updatePlayList(String playlistId, PlaylistUpdateRequest request, UUID ownerId) {

        UpdatePlaylistCommand command = playlistMapper.toCommand(request);
        Playlist playlist = updatePlaylistUseCase.update(playlistId, command, ownerId);

        MoplUserDetails userDetails = (MoplUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        UserSummaryDto owner = new UserSummaryDto(
                userDetails.getId(),
                userDetails.getUserDto().name(),
                userDetails.getUserDto().profilePresignedUrl());
        List<ContentDto> contentDtos = getContentDtos(playlist.getId());
        return PlaylistDto.toDto(playlist, owner, false , contentDtos);
    }

    private List<ContentDto> getContentDtos(UUID playlistId) {
        Map<UUID, List<UUID>> contentIdsMap = getCurationUseCase.getContentIdsByPlaylistIds(Set.of(playlistId));
        List<UUID> contentIds = contentIdsMap.getOrDefault(playlistId, Collections.emptyList());
        if (contentIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<ContentReadModel> contentReadModels = getContentUseCase.getByIds(new HashSet<>(contentIds));
        List<String> thumbnailKeys = contentReadModels.stream()
                .map(ContentReadModel::thumbnailKey)
                .filter(Objects::nonNull)
                .toList();
        Map<String, String> urls = getPresignedUrlUseCase.getPresignedUrls(thumbnailKeys);

        return contentReadModels.stream()
                .map(rm -> ContentDto.from(rm, urls.get(rm.thumbnailKey())))
                .toList();
    }

    public void deletePlaylist(String playlistId, UUID ownerId) {
        deletePlaylistUseCase.delete(playlistId, ownerId);
    }

    public void addContentToPlaylist(String playlistId, String contentId, UUID ownerId) {
        addContentToCurationUseCase.addContentToPlaylist(playlistId, contentId, ownerId);
    }

    public void deleteContentFromPlaylist(String playlistId, String contentId, UUID ownerId) {
        deleteContentFromCurationUseCase.deleteContentFromPlaylist(playlistId, contentId, ownerId);
    }

    public void subscribePlaylist(String playlistId, UUID userId) {
        subscribePlaylistUseCase.subscribe(playlistId, userId);
    }

    public void unsubscribePlaylist(String playlistId, UUID userId) {
        unsubscribePlaylistUseCase.unsubscribe(playlistId, userId);
    }
}
