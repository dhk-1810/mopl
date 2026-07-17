package org.codeit.sb06.team03.mopl.playlist.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.UserSummary;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.security.MoplUserDetails;
import org.codeit.sb06.team03.mopl.content.controller.ContentDto;
import org.codeit.sb06.team03.mopl.image.service.ImageQueryService;
import org.codeit.sb06.team03.mopl.playlist.config.PlaylistReadModel;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.Playlist;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.cqrs.ExternalContentView;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.playlist.config.infra.in.*;
import org.codeit.sb06.team03.mopl.playlist.config.infra.in.request.CursorRequestPlaylistDto;
import org.codeit.sb06.team03.mopl.playlist.config.infra.in.request.PlaylistCreateRequest;
import org.codeit.sb06.team03.mopl.playlist.config.infra.in.request.PlaylistUpdateRequest;
import org.codeit.sb06.team03.mopl.playlist.config.infra.in.response.CursorResponsePlaylistDto;
import org.codeit.sb06.team03.mopl.playlist.config.infra.in.response.PlaylistDto;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PlaylistCompositeService {

    private final PlaylistCommandService playlistCommandService;
    private final ImageQueryService imageQueryService;

    private final PlaylistQueryService playlistQueryService;
    private final ExternalUserQueryService externalUserQueryService;
    private final ExternalContentQueryService externalContentQueryService;

    public PlaylistDto createPlaylist(PlaylistCreateRequest request, UUID ownerId) {
        Playlist playlist = playlistCommandService.create(request.title(), request.description(), ownerId);

        MoplUserDetails userDetails = (MoplUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        var userDto = userDetails.getUserDto();
        UserSummary owner = new UserSummary(userDto.id(), userDto.name(), userDto.profileImageUrl());

        return PlaylistDto.toDto(playlist, owner, false , Collections.emptyList());
    }

    public CursorResponsePlaylistDto getAll(CursorRequestPlaylistDto request, UUID viewerId) {
        Slice<PlaylistReadModel> slice = playlistQueryService.getPlaylists(request, viewerId);
        List<PlaylistReadModel> readModels = slice.getContent();

        List<UUID> ownerIds = readModels.stream().map(PlaylistReadModel::ownerId).toList();
        Map<UUID, ExternalUserView> ownersMap = externalUserQueryService.getProfiles(ownerIds);
        Map<UUID, UserSummary> owners = ownersMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            ExternalUserView profile = entry.getValue();
                            String url = imageQueryService.getPresignedUrl(profile.getProfileImageKey());
                            return new UserSummary(profile.getId(), profile.getName(), url);
                        }
                ));

        Set<UUID> playlistIds = readModels.stream().map(PlaylistReadModel::id).collect(Collectors.toSet());
        Map<UUID, Boolean> subscribedByMe = playlistQueryService.isSubscribed(playlistIds, viewerId);

        // 1. 플레이리스트별 컨텐츠 ID 가져옴 (Curation 로컬 쿼리)
        Map<UUID, List<UUID>> contentIdsMap = playlistQueryService.getContentIdsByPlaylistIds(playlistIds);

        // 2. 컨텐츠 전체를 한번에 조회 및 presigned URL 일괄 생성
        Set<UUID> allContentIds = contentIdsMap.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        List<ExternalContentView> contentViews = externalContentQueryService.getContents(allContentIds);

        List<String> thumbnailKeys = contentViews.stream()
                .map(ExternalContentView::getThumbnailKey)
                .filter(Objects::nonNull)
                .toList();
        Map<String, String> urls = imageQueryService.getPresignedUrls(thumbnailKeys);

        List<ContentDto> contentDtos = contentViews.stream()
                .map(cv -> new ContentDto(
                        cv.getId(),
                        cv.getType(),
                        cv.getTitle(),
                        cv.getDescription(),
                        urls.get(cv.getThumbnailKey()),
                        parseTags(cv.getTags()),
                        cv.getAverageRating(),
                        cv.getReviewCount(),
                        cv.getWatcherCount()
                ))
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
        PlaylistReadModel readModel = playlistQueryService.getPlaylist(playlistId, viewerId);

        ExternalUserView ownerProfile = externalUserQueryService.getProfile(readModel.ownerId());
        UserSummary owner = null;
        if (ownerProfile != null) {
            String url = imageQueryService.getPresignedUrl(ownerProfile.getProfileImageKey());
            owner = new UserSummary(ownerProfile.getId(), ownerProfile.getName(), url);
        }

        boolean subscribedByMe = playlistQueryService.isSubscribed(playlistId, viewerId);
        List<ContentDto> contentDtos = getContentDtos(readModel.id());
        return PlaylistDto.toDto(readModel, owner, subscribedByMe, contentDtos);
    }

    public PlaylistDto updatePlayList(UUID playlistId, PlaylistUpdateRequest request, UUID ownerId) {
        Playlist playlist = playlistCommandService.update(playlistId, request.title(), request.description(), ownerId);

        MoplUserDetails userDetails = (MoplUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        var userDto = userDetails.getUserDto();
        UserSummary owner = new UserSummary(userDto.id(), userDto.name(), userDto.profileImageUrl());
        List<ContentDto> contentDtos = getContentDtos(playlist.getId());
        return PlaylistDto.toDto(playlist, owner, false , contentDtos);
    }

    public void deletePlaylist(UUID playlistId, UUID ownerId) {
        playlistCommandService.delete(playlistId, ownerId);
    }

    public void addContentToPlaylist(UUID playlistId, UUID contentId, UUID ownerId) {
        ExternalContentView content = externalContentQueryService.getContent(contentId);
        String title = content != null ? content.getTitle() : "Unknown Content";
        playlistCommandService.addContentToPlaylist(playlistId, contentId, title, ownerId);
    }

    public void deleteContentFromPlaylist(UUID playlistId, UUID contentId, UUID ownerId) {
        playlistCommandService.deleteContentFromPlaylist(playlistId, contentId, ownerId);
    }

    public void subscribePlaylist(UUID playlistId, UUID userId) {
        playlistCommandService.subscribe(playlistId, userId);
    }

    public void unsubscribePlaylist(UUID playlistId, UUID userId) {
        playlistCommandService.unsubscribe(playlistId, userId);
    }

    private List<ContentDto> getContentDtos(UUID playlistId) {
        Map<UUID, List<UUID>> contentIdsMap = playlistQueryService.getContentIdsByPlaylistIds(Set.of(playlistId));
        List<UUID> contentIds = contentIdsMap.getOrDefault(playlistId, Collections.emptyList());
        if (contentIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<ExternalContentView> contentViews = externalContentQueryService.getContents(contentIds);
        List<String> thumbnailKeys = contentViews.stream()
                .map(ExternalContentView::getThumbnailKey)
                .filter(Objects::nonNull)
                .toList();
        Map<String, String> urls = imageQueryService.getPresignedUrls(thumbnailKeys);

        return contentViews.stream()
                .map(cv -> new ContentDto(
                        cv.getId(),
                        cv.getType(),
                        cv.getTitle(),
                        cv.getDescription(),
                        urls.get(cv.getThumbnailKey()),
                        parseTags(cv.getTags()),
                        cv.getAverageRating(),
                        cv.getReviewCount(),
                        cv.getWatcherCount()
                ))
                .toList();
    }

    private Set<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet());
    }
}
