package org.codeit.sb06.team03.mopl.playlist.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.domain.exception.InvalidIdentifierException;
import org.codeit.sb06.team03.mopl.playlist.PlaylistReadModel;
import org.codeit.sb06.team03.mopl.playlist.application.in.GetCurationUseCase;
import org.codeit.sb06.team03.mopl.playlist.application.in.GetSinglePlaylistUseCase;
import org.codeit.sb06.team03.mopl.playlist.application.in.GetPlaylistsUseCase;
import org.codeit.sb06.team03.mopl.playlist.application.in.GetSubscriptionUseCase;
import org.codeit.sb06.team03.mopl.playlist.application.out.LoadCurationPort;
import org.codeit.sb06.team03.mopl.playlist.application.out.LoadSinglePlaylistPort;
import org.codeit.sb06.team03.mopl.playlist.application.out.LoadPlaylistsPort;
import org.codeit.sb06.team03.mopl.playlist.application.out.LoadSubscriptionPort;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.Playlist;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.SubscriptionId;
import org.codeit.sb06.team03.mopl.playlist.domain.exception.PlaylistNotFoundException;
import org.codeit.sb06.team03.mopl.playlist.infra.in.request.CursorRequestPlaylistDto;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PlaylistQueryService implements GetPlaylistsUseCase, GetSinglePlaylistUseCase, GetSubscriptionUseCase, GetCurationUseCase {

    private final LoadPlaylistsPort loadPlaylistsPort;
    private final LoadSinglePlaylistPort loadSinglePlaylistPort;
    private final LoadCurationPort loadCurationPort;
    private final LoadSubscriptionPort loadSubscriptionPort;

    @Override
    public Slice<PlaylistReadModel> get(CursorRequestPlaylistDto request, UUID viewerId) {

        String keywordLike = request.keywordLike();
        UUID ownerIdEqual = parseUUID(request.ownerIdEqual());
        UUID subscriberIdEqual = parseUUID(request.subscriberIdEqual());
        String cursor = StringUtils.hasText(request.cursor()) ? request.cursor() : null;
        UUID idAfter = parseUUID(request.idAfter());
        int limit = request.limit();
        String sortDirection = request.sortDirection();
        String sortBy = request.sortBy();

        return loadPlaylistsPort.findAll(
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

    @Override
    public PlaylistReadModel get(String playlistId, UUID viewerId) {

        UUID playlistUUID = parseUUID(playlistId);
        Playlist playlist = loadSinglePlaylistPort.findById(playlistUUID)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistUUID));
        return PlaylistReadModel.from(playlist);
    }

    @Override
    public boolean isSubscribed(String playlistId, UUID viewerId) {

        UUID playlistUUID = parseUUID(playlistId);
        SubscriptionId id = new SubscriptionId(playlistUUID, viewerId);
        return loadSubscriptionPort.existsById(id);
    };

    @Override
    public Map<UUID, Boolean> isSubscribed(List<UUID> playlistIds, UUID viewerId) {
        return loadSubscriptionPort.existsByIdIn(playlistIds, viewerId);
    }

    @Override
    public Map<UUID, List<UUID>> getContentIdsByPlaylistIds(List<UUID> playlistIds) {
        return loadCurationPort.findAllByPlaylistIdsIn(playlistIds);
    }

    private UUID parseUUID(String id) {
        if (!StringUtils.hasText(id)) {
            return null;
        }
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidIdentifierException(id);
        }
    }


}
