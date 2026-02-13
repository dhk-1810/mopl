package org.codeit.sb06.team03.mopl.playlist.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.application.out.LoadAccountPort;
import org.codeit.sb06.team03.mopl.account.domain.Account;
import org.codeit.sb06.team03.mopl.account.domain.exception.AccountNotFoundException;
import org.codeit.sb06.team03.mopl.account.domain.exception.InvalidIdentifierException;
import org.codeit.sb06.team03.mopl.playlist.application.in.GetSinglePlaylistUseCase;
import org.codeit.sb06.team03.mopl.playlist.application.in.GetPlaylistsUseCase;
import org.codeit.sb06.team03.mopl.playlist.application.out.LoadCurationPort;
import org.codeit.sb06.team03.mopl.playlist.application.out.LoadSinglePlaylistPort;
import org.codeit.sb06.team03.mopl.playlist.application.out.LoadPlaylistsPort;
import org.codeit.sb06.team03.mopl.playlist.application.out.LoadSubscriptionPort;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.Playlist;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.SubscriptionId;
import org.codeit.sb06.team03.mopl.playlist.domain.exception.PlaylistNotFoundException;
import org.codeit.sb06.team03.mopl.playlist.infra.in.CursorRequestPlaylistDto;
import org.codeit.sb06.team03.mopl.playlist.infra.in.CursorResponsePlaylistDto;
import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistDto;
import org.codeit.sb06.team03.mopl.playlist.infra.in.UserSummaryDto;
import org.codeit.sb06.team03.mopl.user.domain.Profile;
import org.codeit.sb06.team03.mopl.user.infra.in.UserDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PlaylistQueryService implements GetPlaylistsUseCase, GetSinglePlaylistUseCase {

    private final LoadPlaylistsPort loadPlaylistsPort;
    private final LoadSinglePlaylistPort loadSinglePlaylistPort;
//    private final LoadContentPort loadContentPort;
    private final LoadCurationPort loadCurationPort;
    private final LoadSubscriptionPort loadSubscriptionPort;

    @Override
    public CursorResponsePlaylistDto get(CursorRequestPlaylistDto request) {

        final String keywordLike = request.keywordLike();
        final UUID ownerIdEqual = (request.ownerIdEqual() == null) ? null : parseUUID(request.ownerIdEqual());
        final UUID subscriberIdEqual = (request.subscriberIdEqual() == null) ? null : parseUUID(request.subscriberIdEqual());
        final String cursor = request.cursor();
        final UUID idAfter =  (request.idAfter() == null) ? null : parseUUID(request.idAfter());
        final int limit = request.limit();
        final String sortDirection = request.sortDirection();
        final String sortBy = request.sortBy();

        loadPlaylistsPort.findAll(
                keywordLike,
                ownerIdEqual,
                subscriberIdEqual,
                cursor,
                idAfter,
                limit,
                sortDirection,
                sortBy
        );

        return null;
    }

    @Override
    public PlaylistDto get(String playlistId, UUID viewerId) {

        UUID playlistUUID = parseUUID(playlistId);
        Playlist playlist = loadSinglePlaylistPort.findById(playlistUUID)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistUUID));

        List<UUID> contentIds = loadCurationPort.findAllByPlaylistId(playlistUUID);

        // TODO
//      UserDto owner = getUserDto(playlist.ownerId);
//      List<ContentDto> contents = getContents(playlistUUID);
        SubscriptionId id = new SubscriptionId(playlistUUID, viewerId);
        boolean subscribedByMe = loadSubscriptionPort.existsById(id);

//      List<ContentDto> contents = loadContentsPort.findAllByIdIn()
//                .stream().map(ContentDto::toDto).toList();

        return PlaylistDto.toDto(playlist, null, subscribedByMe);
    }

    private UserSummaryDto getUserDto(UUID ownerId){
//        Profile profile = loadProfilePort.findById(ownerId)
//                .orElseThrow(() -> new ProfileNotFoundException());
//        return new UserSummaryDto(
//                ownerId,
//                profile.getName(),
//                profile.getImage().url()
//        );
        return null;
    }

//    private List<ContentDto> getContents(playlistUUID){
//        List<UUID> contentIds = loadCurationPort.findAllByPlaylistId();
//        return loadContentsPort.findAllByIdIn(contentIds)
//                .stream().map(ContentDto::toDto).toList();
//    }

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidIdentifierException(id);
        }
    }


}
