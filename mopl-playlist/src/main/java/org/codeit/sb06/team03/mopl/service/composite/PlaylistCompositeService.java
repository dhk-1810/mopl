package org.codeit.sb06.team03.mopl.service.composite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.dto.ProfileReadModel;
import org.codeit.sb06.team03.mopl.dto.UserSummary;
import org.codeit.sb06.team03.mopl.enums.ContentType;
import org.codeit.sb06.team03.mopl.enums.SortDirection;
import org.codeit.sb06.team03.mopl.dto.PlaylistReadModel;
import org.codeit.sb06.team03.mopl.dto.request.CursorRequestPlaylistDto;
import org.codeit.sb06.team03.mopl.dto.request.PlaylistCreateRequest;
import org.codeit.sb06.team03.mopl.dto.request.PlaylistUpdateRequest;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponsePlaylistDto;
import org.codeit.sb06.team03.mopl.dto.response.PlaylistDto;
import org.codeit.sb06.team03.mopl.dto.response.ContentDto;
import org.codeit.sb06.team03.mopl.entity.Playlist;
import org.codeit.sb06.team03.mopl.service.ImageQueryService;
import org.codeit.sb06.team03.mopl.service.ProfileQueryService;
import org.codeit.sb06.team03.mopl.service.application.PlaylistCommandService;
import org.codeit.sb06.team03.mopl.service.PlaylistQueryService;
import org.codeit.sb06.team03.mopl.service.application.ContentQueryService;
import org.codeit.sb06.team03.mopl.entity.ContentReadModel;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PlaylistCompositeService {

    private final PlaylistCommandService playlistCommandService;
    private final PlaylistQueryService playlistQueryService;
    private final ProfileQueryService profileQueryService;
    private final ImageQueryService imageQueryService;
    private final ContentQueryService contentQueryService;

    public PlaylistDto createPlaylist(PlaylistCreateRequest request, UUID ownerId) {
        Playlist playlist = playlistCommandService.create(request.title(), request.description(), ownerId);

        UserSummary owner = getUserSummary(ownerId);
        return PlaylistDto.toDto(playlist, owner, false , Collections.emptyList());
    }

    public CursorResponsePlaylistDto getAll(CursorRequestPlaylistDto request, UUID viewerId) {
        Slice<PlaylistReadModel> slice = playlistQueryService.getPlaylists(request, viewerId);
        List<PlaylistReadModel> readModels = slice.getContent();

        List<UUID> ownerIds = readModels.stream().map(PlaylistReadModel::ownerId).toList();
        Map<UUID, ProfileReadModel> ownersMap = profileQueryService.getProfileReadModels(ownerIds);
        Map<UUID, UserSummary> owners = ownerIds.stream().distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> getUserSummary(ownersMap.get(id), id)
                ));

        Set<UUID> playlistIds = readModels.stream().map(PlaylistReadModel::id).collect(Collectors.toSet());
        Map<UUID, Boolean> subscribedByMe = playlistQueryService.isSubscribed(playlistIds, viewerId);

        // 1. 플레이리스트별 컨텐츠 ID 가져옴 (Curation 로컬 쿼리)
        Map<UUID, List<UUID>> contentIdsMap = playlistQueryService.getContentIdsByPlaylistIds(playlistIds);

        // 2. 컨텐츠 전체를 한번에 조회 및 presigned URL 일괄 생성
        Set<UUID> allContentIds = contentIdsMap.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        List<ContentReadModel> contents = allContentIds.isEmpty() ? Collections.emptyList() : contentQueryService.getByIds(allContentIds);

        List<String> s3Keys = contents.stream()
                .map(ContentReadModel::thumbnailKey)
                .filter(key -> key != null && !key.startsWith("http://") && !key.startsWith("https://"))
                .toList();
        Map<String, String> urls = imageQueryService.getPresignedUrls(s3Keys);

        List<ContentDto> contentDtos = contents.stream()
                .map(c -> {
                    String key = c.thumbnailKey();
                    String url = (key != null && (key.startsWith("http://") || key.startsWith("https://")))
                            ? key
                            : urls.get(key);
                    return new ContentDto(
                            c.id(),
                            c.type(),
                            c.title(),
                            c.description(),
                            url,
                            c.tags() != null ? c.tags() : Collections.emptySet(),
                            c.averageRating(),
                            c.reviewCount(),
                            c.watcherCount()
                    );
                })
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

        if (readModels.isEmpty()) {
            return new CursorResponsePlaylistDto(
                    Collections.emptyList(),
                    null,
                    null,
                    false,
                    0,
                    request.sortBy(),
                    SortDirection.parse(request.sortDirection())
            );
        }

        List<PlaylistDto> data = readModels.stream()
                .map(readModel -> PlaylistDto.toDto(
                        readModel,
                        owners.computeIfAbsent(readModel.ownerId(), this::getUserSummary),
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
                data.size(),
                request.sortBy(),
                SortDirection.parse(request.sortDirection())
        );
    }

    public PlaylistDto get(UUID playlistId, UUID viewerId) {
        PlaylistReadModel readModel = playlistQueryService.getPlaylist(playlistId, viewerId);

        UserSummary owner = getUserSummary(readModel.ownerId());
        boolean subscribedByMe = playlistQueryService.isSubscribed(playlistId, viewerId);
        List<ContentDto> contentDtos = getContentDtos(readModel.id());
        return PlaylistDto.toDto(readModel, owner, subscribedByMe, contentDtos);
    }

    public PlaylistDto updatePlayList(UUID playlistId, PlaylistUpdateRequest request, UUID ownerId) {
        Playlist playlist = playlistCommandService.update(playlistId, request.title(), request.description(), ownerId);

        UserSummary owner = getUserSummary(ownerId);
        List<ContentDto> contentDtos = getContentDtos(playlist.getId());
        return PlaylistDto.toDto(playlist, owner, false , contentDtos);
    }

    public void deletePlaylist(UUID playlistId, UUID ownerId) {
        playlistCommandService.delete(playlistId, ownerId);
    }

    public void addContentToPlaylist(UUID playlistId, UUID contentId, UUID ownerId) {
        ContentReadModel content = null;
        try {
            content = contentQueryService.get(contentId);
        } catch (Exception ignored) {
        }
        String title = content != null ? content.title() : "Unknown Content";
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

        List<ContentReadModel> contents = contentQueryService.getByIds(new HashSet<>(contentIds));

        List<String> s3Keys = contents.stream()
                .map(ContentReadModel::thumbnailKey)
                .filter(key -> key != null && !key.startsWith("http://") && !key.startsWith("https://"))
                .toList();
        Map<String, String> urls = imageQueryService.getPresignedUrls(s3Keys);

        return contents.stream()
                .map(c -> {
                    String key = c.thumbnailKey();
                    String url = (key != null && (key.startsWith("http://") || key.startsWith("https://")))
                            ? key
                            : urls.get(key);
                    return new ContentDto(
                            c.id(),
                            c.type(),
                            c.title(),
                            c.description(),
                            url,
                            c.tags() != null ? c.tags() : Collections.emptySet(),
                            c.averageRating(),
                            c.reviewCount(),
                            c.watcherCount()
                    );
                })
                .toList();
    }

    private UserSummary getUserSummary(UUID ownerId) {
        String ownerName = "Unknown User";
        String ownerUrl = null;
        try {
            ProfileReadModel ownerProfile = profileQueryService.getProfileReadModel(ownerId);
            if (ownerProfile != null) {
                ownerName = ownerProfile.name();
                ownerUrl = imageQueryService.getPresignedUrl(ownerProfile.imageKey());
            }
        } catch (Exception ignored) {
        }
        return new UserSummary(ownerId, ownerName, ownerUrl);
    }

    private UserSummary getUserSummary(ProfileReadModel ownerProfile, UUID ownerId) {
        String ownerName = "Unknown User";
        String ownerUrl = null;
        if (ownerProfile != null) {
            ownerName = ownerProfile.name();
            ownerUrl = imageQueryService.getPresignedUrl(ownerProfile.imageKey());
        }
        return new UserSummary(ownerId, ownerName, ownerUrl);
    }
}
