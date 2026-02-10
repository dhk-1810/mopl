package org.codeit.sb06.team03.mopl.playlist.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.application.out.LoadAccountPort;
import org.codeit.sb06.team03.mopl.account.domain.Account;
import org.codeit.sb06.team03.mopl.account.domain.exception.AccountNotFoundException;
import org.codeit.sb06.team03.mopl.account.domain.exception.InvalidIdentifierException;
import org.codeit.sb06.team03.mopl.playlist.application.in.GetPlaylistUseCase;
import org.codeit.sb06.team03.mopl.playlist.application.out.LoadPlaylistPort;
import org.codeit.sb06.team03.mopl.playlist.domain.Playlist;
import org.codeit.sb06.team03.mopl.playlist.domain.exception.PlaylistNotFoundException;
import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistDto;
import org.codeit.sb06.team03.mopl.user.infra.in.UserDto;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PlaylistQueryService implements GetPlaylistUseCase {

    private final LoadPlaylistPort loadPlaylistPort;
    private final LoadAccountPort loadAccountPort;

    @Override
    public PlaylistDto get(String playlistId, UUID viewerId) {
        UUID accountUUID = parseUUID(playlistId);
        Playlist playlist = loadPlaylistPort.findById(accountUUID)
                .orElseThrow(() -> new PlaylistNotFoundException(accountUUID));

        Account account = loadAccountPort.findById(viewerId)
                .orElseThrow(() -> new AccountNotFoundException(accountUUID));

//        List<ContentsDto> contents = playlist.getContents()
//                .stream().map(ContentsMapper::toDto).toList();

        return new PlaylistDto(
                playlist.getId(),
                null, // TODO
                playlist.getTitle(),
                playlist.getDescription(),
                playlist.getUpdatedAt(),
                playlist.getSubscriberCount(),
                false // TODO
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
