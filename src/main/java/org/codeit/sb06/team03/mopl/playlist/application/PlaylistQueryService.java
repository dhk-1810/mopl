package org.codeit.sb06.team03.mopl.playlist.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.application.out.LoadAccountPort;
import org.codeit.sb06.team03.mopl.account.domain.Account;
import org.codeit.sb06.team03.mopl.account.domain.exception.AccountNotFoundException;
import org.codeit.sb06.team03.mopl.account.domain.exception.InvalidIdentifierException;
import org.codeit.sb06.team03.mopl.playlist.application.in.GetSinglePlaylistUseCase;
import org.codeit.sb06.team03.mopl.playlist.application.in.GetPlaylistsUseCase;
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
import org.codeit.sb06.team03.mopl.user.infra.in.UserDto;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PlaylistQueryService implements GetPlaylistsUseCase, GetSinglePlaylistUseCase {

    private final LoadPlaylistsPort loadPlaylistsPort;
    private final LoadSinglePlaylistPort loadSinglePlaylistPort;
//    private final LoadContentPort loadContentPort;
    private final LoadSubscriptionPort loadSubscriptionPort;
    private final LoadAccountPort loadAccountPort;

    @Override
    public CursorResponsePlaylistDto get(CursorRequestPlaylistDto request) {

        String keywordLike = request.keywordLike();
        UUID ownerIdEqual = parseUUID(request.ownerIdEqual());
        UUID subscriberIdEqual = parseUUID(request.subscriberIdEqual());
        final String idAfter = request.idAfter();
        final Integer limit = request.limit();
        final String sortDirection = request.sortDirection();
        final String sortBy = request.sortBy();

        loadPlaylistsPort

        return new CursorResponsePlaylistDto(

        );
    }

    @Override
    public PlaylistDto get(String playlistId, UUID viewerId) {
        UUID playlistUUID = parseUUID(playlistId);
        Playlist playlist = loadSinglePlaylistPort.findById(playlistUUID)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistUUID));

        Account account = loadAccountPort.findById(viewerId)
                .orElseThrow(() -> new AccountNotFoundException(viewerId));
        UserSummaryDto owner = new UserSummaryDto(
                account.getId(),
                null, // TODO 유저네임,
                null // TODO 프로필URL
        );

//        List<ContentsDto> contents = playlist.getContents()
//                .stream().map(ContentsMapper::toDto).toList();

        SubscriptionId id = new SubscriptionId(playlistUUID, account.getId());
        boolean subscribedByMe = loadSubscriptionPort.existsById(id);

        return new PlaylistDto(
                playlist.getId(),
                owner,
                playlist.getTitle(),
                playlist.getDescription(),
                playlist.getUpdatedAt(),
                playlist.getSubscriberCount(),
                subscribedByMe
        );
    }

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidIdentifierException(id);
        }
    }


}
