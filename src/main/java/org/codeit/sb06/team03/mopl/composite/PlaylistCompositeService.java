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
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummary;
import org.codeit.sb06.team03.mopl.profile.ProfileReadModel;

import org.codeit.sb06.team03.mopl.profile.application.in.GetProfileUseCase;

import org.springframework.data.domain.Slice;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PlaylistCompositeService {

    private final PlaylistMapper playlistMapper;
    private final CreatePlaylistUseCase createPlaylistUseCase;
    private final GetPlaylistUseCase getPlaylistUseCase;
    private final UpdatePlaylistUseCase updatePlaylistUseCase;
    private final DeletePlaylistUseCase deletePlaylistUseCase;

    private final AddCurationUseCase addCurationUseCase;
    private final DeleteCurationUseCase deleteCurationUseCase;

    private final SubscribePlaylistUseCase subscribePlaylistUseCase;
    private final UnsubscribePlaylistUseCase unsubscribePlaylistUseCase;

    private final GetProfileUseCase getProfileUseCase;
    private final GetSubscriptionUseCase getSubscriptionUseCase;

    private final GetContentUseCase getContentUseCase;
    private final GetCurationUseCase getCurationUseCase;
    private final GetPresignedUrlUseCase getPresignedUrlUseCase;

    public PlaylistDto createPlaylist(PlaylistCreateRequest request, UUID ownerId) {

        CreatePlaylistCommand command = playlistMapper.toCommand(request);
        Playlist playlist = createPlaylistUseCase.create(command, ownerId);

        MoplUserDetails userDetails = (MoplUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        var userDto = userDetails.getUserDto();
        UserSummary owner = new UserSummary(userDto.id(), userDto.name(), userDto.profileImageUrl());

        return PlaylistDto.toDto(playlist, owner, false , Collections.emptyList());
    }

    public CursorResponsePlaylistDto getAll(CursorRequestPlaylistDto request, UUID viewerId) {
        Slice<PlaylistReadModel> slice = getPlaylistUseCase.get(request, viewerId);
        List<PlaylistReadModel> readModels = slice.getContent();

        List<UUID> ownerIds = readModels.stream().map(PlaylistReadModel::ownerId).toList();
        Map<UUID, ProfileReadModel> ownersMap = getProfileUseCase.getProfileReadModels(ownerIds);
        Map<UUID, UserSummary> owners = ownersMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            ProfileReadModel profile = entry.getValue();
                            String url = getPresignedUrlUseCase.getPresignedUrl(profile.imageKey());
                            return new UserSummary(profile.userId(), profile.name(), url);
                        }
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

    public PlaylistDto get(UUID playlistId, UUID viewerId) {
        PlaylistReadModel readModel = getPlaylistUseCase.get(playlistId, viewerId);

        ProfileReadModel ownerProfile = getProfileUseCase.getProfileReadModel(readModel.ownerId());
        String url = getPresignedUrlUseCase.getPresignedUrl(ownerProfile.imageKey());
        UserSummary owner = new UserSummary(ownerProfile.userId(), ownerProfile.name(), url);

        boolean subscribedByMe = getSubscriptionUseCase.isSubscribed(playlistId, viewerId);
        List<ContentDto> contentDtos = getContentDtos(readModel.id());
        return PlaylistDto.toDto(readModel, owner, subscribedByMe, contentDtos);
    }

    public PlaylistDto updatePlayList(UUID playlistId, PlaylistUpdateRequest request, UUID ownerId) {

        UpdatePlaylistCommand command = playlistMapper.toCommand(request);
        Playlist playlist = updatePlaylistUseCase.update(playlistId, command, ownerId);

        MoplUserDetails userDetails = (MoplUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        var userDto = userDetails.getUserDto();
        UserSummary owner = new UserSummary(userDto.id(), userDto.name(), userDto.profileImageUrl());
        List<ContentDto> contentDtos = getContentDtos(playlist.getId());
        return PlaylistDto.toDto(playlist, owner, false , contentDtos);
    }

    public void deletePlaylist(UUID playlistId, UUID ownerId) {
        deletePlaylistUseCase.delete(playlistId, ownerId);
    }


    public void addContentToPlaylist(UUID playlistId, UUID contentId, UUID ownerId) {
        ContentReadModel content = getContentUseCase.get(contentId);
        addCurationUseCase.addContentToPlaylist(playlistId, contentId, content.title(), ownerId);
    }

    public void deleteContentFromPlaylist(UUID playlistId, UUID contentId, UUID ownerId) {
        deleteCurationUseCase.deleteContentFromPlaylist(playlistId, contentId, ownerId);
    }


    public void subscribePlaylist(UUID playlistId, UUID userId) {
        subscribePlaylistUseCase.subscribe(playlistId, userId);
    }

    public void unsubscribePlaylist(UUID playlistId, UUID userId) {
        unsubscribePlaylistUseCase.unsubscribe(playlistId, userId);
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
}
