package org.codeit.sb06.team03.mopl.playlist.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.playlist.PlaylistReadModel;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.Playlist;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.SubscriptionId;
import org.codeit.sb06.team03.mopl.playlist.domain.exception.PlaylistNotFoundException;
import org.codeit.sb06.team03.mopl.playlist.infra.in.request.CursorRequestPlaylistDto;
import org.codeit.sb06.team03.mopl.playlist.infra.out.CurationRepository;
import org.codeit.sb06.team03.mopl.playlist.infra.out.PlaylistRepository;
import org.codeit.sb06.team03.mopl.playlist.infra.out.SubscriptionRepository;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@RequiredArgsConstructor
@Service
@Transactional(value = "playlistTransactionManager", readOnly = true)
public class PlaylistQueryService {

    private final PlaylistRepository playlistRepository;
    private final CurationRepository curationRepository;
    private final SubscriptionRepository subscriptionRepository;

    // 1. Playlist Queries
    public Slice<PlaylistReadModel> getPlaylists(CursorRequestPlaylistDto request, UUID viewerId) {
        String keywordLike = request.keywordLike();
        UUID ownerIdEqual = request.ownerIdEqual();
        UUID subscriberIdEqual = request.subscriberIdEqual();
        String cursor = StringUtils.hasText(request.cursor()) ? request.cursor() : null;
        UUID idAfter = request.idAfter();
        int limit = request.limit();
        String sortDirection = request.sortDirection();
        String sortBy = request.sortBy();

        return playlistRepository.findAll(
                keywordLike,
                ownerIdEqual,
                subscriberIdEqual,
                cursor,
                idAfter,
                limit,
                sortDirection,
                sortBy
        );
    }

    public PlaylistReadModel getPlaylist(UUID playlistId, UUID viewerId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistId));
        return PlaylistReadModel.from(playlist);
    }

    // 2. Subscription Queries
    public boolean isSubscribed(UUID playlistId, UUID viewerId) {
        SubscriptionId id = new SubscriptionId(playlistId, viewerId);
        return subscriptionRepository.existsById(id);
    }

    public Map<UUID, Boolean> isSubscribed(Set<UUID> playlistIds, UUID viewerId) {
        return subscriptionRepository.findAllSubscribedMap(playlistIds, viewerId);
    }

    // 3. Curation Queries
    public Map<UUID, List<UUID>> getContentIdsByPlaylistIds(Set<UUID> playlistIds) {
        return curationRepository.findAllByPlaylistIdsIn(playlistIds);
    }
}
