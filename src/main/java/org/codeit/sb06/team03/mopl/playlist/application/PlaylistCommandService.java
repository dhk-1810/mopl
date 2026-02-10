package org.codeit.sb06.team03.mopl.playlist.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.domain.exception.InvalidIdentifierException;
import org.codeit.sb06.team03.mopl.playlist.application.in.*;
import org.codeit.sb06.team03.mopl.playlist.application.out.LoadPlaylistPort;
import org.codeit.sb06.team03.mopl.playlist.application.out.SavePlaylistPort;
import org.codeit.sb06.team03.mopl.playlist.domain.Playlist;
import org.codeit.sb06.team03.mopl.playlist.domain.PlaylistService;
import org.codeit.sb06.team03.mopl.playlist.domain.event.PlaylistEvent;
import org.codeit.sb06.team03.mopl.playlist.domain.exception.PlaylistNotFoundException;
import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistDto;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PlaylistCommandService implements CreatePlaylistUseCase, UpdatePlaylistUseCase, DeletePlaylistUseCase {

    private final SavePlaylistPort savePlaylistPort;
    private final LoadPlaylistPort loadPlaylistPort;
    private final PlaylistService playlistService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Playlist create(CreatePlaylistCommand command, UUID ownerId) {

        final String title = command.title();
        final String description = command.description();

        Playlist newPlaylist = playlistService.create(title, description, ownerId);
        savePlaylistPort.save(newPlaylist);

         eventPublisher.publishEvent(new PlaylistEvent.PlaylistCreatedEvent(ownerId));
        return newPlaylist;
    }

    @Override
    // TODO 소유자 검증
    public Playlist update(String playlistId, UpdatePlaylistCommand command, UUID ownerId) {

        UUID playlistUUID = parseUUID(playlistId);
        final String title = command.title();
        final String description = command.description();

        Playlist playlist = loadPlaylistPort.findById(playlistUUID)
                        .orElseThrow(() -> new PlaylistNotFoundException(playlistUUID));
        playlist = playlistService.update(playlist, title, description);
        savePlaylistPort.save(playlist);
        return playlist;
    }

    @Override
    // TODO 소유자 검증
    public void delete(String playlistId, UUID ownerId) {
        UUID playlistUUID = parseUUID(playlistId);
        loadPlaylistPort.findById(playlistUUID)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistUUID));
        savePlaylistPort.delete(playlistUUID);
    }

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidIdentifierException(id);
        }
    }
}
