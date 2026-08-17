package org.codeit.sb06.team03.mopl.service.composite;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dto.UserSummary;
import org.codeit.sb06.team03.mopl.enums.SortDirection;
import org.codeit.sb06.team03.mopl.dto.PlaylistReadModel;
import org.codeit.sb06.team03.mopl.dto.request.CursorRequestPlaylistDto;
import org.codeit.sb06.team03.mopl.dto.request.PlaylistCreateRequest;
import org.codeit.sb06.team03.mopl.dto.request.PlaylistUpdateRequest;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponsePlaylistDto;
import org.codeit.sb06.team03.mopl.dto.response.PlaylistDto;
import org.codeit.sb06.team03.mopl.dto.response.ContentDto;
import org.codeit.sb06.team03.mopl.entity.Playlist;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalContentView;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.service.cqrs.ExternalImageQueryService;
import org.codeit.sb06.team03.mopl.service.application.PlaylistCommandService;
import org.codeit.sb06.team03.mopl.service.cqrs.ExternalContentQueryService;
import org.codeit.sb06.team03.mopl.service.cqrs.ExternalUserQueryService;
import org.codeit.sb06.team03.mopl.service.PlaylistQueryService;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.event.CurationContentRequestEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PlaylistCompositeService {

    private final PlaylistCommandService playlistCommandService;
    private final PlaylistQueryService playlistQueryService;
    private final ExternalUserQueryService externalUserQueryService;
    private final ExternalContentQueryService externalContentQueryService;
    private final ExternalImageQueryService imageQueryService;
    private final RabbitTemplate rabbitTemplate;

    public PlaylistDto createPlaylist(PlaylistCreateRequest request, UUID ownerId) {
        Playlist playlist = playlistCommandService.create(request.title(), request.description(), ownerId);

        UserSummary owner = getUserSummary(ownerId);
        return PlaylistDto.toDto(playlist, owner, false , Collections.emptyList());
    }

    public CursorResponsePlaylistDto getAll(CursorRequestPlaylistDto request, UUID viewerId) {
        Slice<PlaylistReadModel> slice = playlistQueryService.getPlaylists(request, viewerId);
        List<PlaylistReadModel> readModels = slice.getContent();

        List<UUID> ownerIds = readModels.stream().map(PlaylistReadModel::ownerId).toList();
        Map<UUID, ExternalUserView> ownersMap = externalUserQueryService.getProfiles(ownerIds);
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

        List<ExternalContentView> contentViews = externalContentQueryService.getContents(allContentIds);

        List<String> s3Keys = contentViews.stream()
                .map(ExternalContentView::getThumbnailKey)
                .filter(key -> key != null && !key.startsWith("http://") && !key.startsWith("https://"))
                .toList();
        Map<String, String> urls = imageQueryService.getPresignedUrls(s3Keys);

        List<ContentDto> contentDtos = contentViews.stream()
                .map(cv -> {
                    String key = cv.getThumbnailKey();
                    String url = (key != null && (key.startsWith("http://") || key.startsWith("https://")))
                            ? key
                            : urls.get(key);
                    return new ContentDto(
                            cv.getId(),
                            cv.getType(),
                            cv.getTitle(),
                            cv.getDescription(),
                            url,
                            parseTags(cv.getTags()),
                            cv.getAverageRating(),
                            cv.getReviewCount(),
                            cv.getWatcherCount()
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
        Set<UUID> foundIds = contentViews.stream().map(ExternalContentView::getId).collect(Collectors.toSet());

        List<String> missingIds = contentIds.stream()
                .filter(id -> !foundIds.contains(id))
                .map(UUID::toString)
                .toList();

        if (!missingIds.isEmpty()) {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.PLAYLIST_EXCHANGE,
                    RabbitConfig.ROUTING_KEY_CURATION_CONTENT_REQUEST,
                    new CurationContentRequestEvent(missingIds)
            );
        }

        List<String> s3Keys = contentViews.stream()
                .map(ExternalContentView::getThumbnailKey)
                .filter(key -> key != null && !key.startsWith("http://") && !key.startsWith("https://"))
                .toList();
        Map<String, String> urls = imageQueryService.getPresignedUrls(s3Keys);

        return contentViews.stream()
                .map(cv -> {
                    String key = cv.getThumbnailKey();
                    String url = (key != null && (key.startsWith("http://") || key.startsWith("https://")))
                            ? key
                            : urls.get(key);
                    return new ContentDto(
                            cv.getId(),
                            cv.getType(),
                            cv.getTitle(),
                            cv.getDescription(),
                            url,
                            parseTags(cv.getTags()),
                            cv.getAverageRating(),
                            cv.getReviewCount(),
                            cv.getWatcherCount()
                    );
                })
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

    private UserSummary getUserSummary(UUID ownerId) {
        ExternalUserView ownerProfile = externalUserQueryService.getProfile(ownerId);
        return getUserSummary(ownerProfile, ownerId);
    }

    private UserSummary getUserSummary(ExternalUserView ownerProfile, UUID ownerId) {
        String ownerName = "Unknown User";
        String ownerUrl = null;
        if (ownerProfile != null) {
            ownerName = ownerProfile.getName();
            ownerUrl = imageQueryService.getPresignedUrl(ownerProfile.getProfileImageKey());
        }
        return new UserSummary(ownerId, ownerName, ownerUrl);
    }
}
