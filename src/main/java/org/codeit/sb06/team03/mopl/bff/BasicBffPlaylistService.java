package org.codeit.sb06.team03.mopl.bff;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.playlist.application.in.*;
import org.codeit.sb06.team03.mopl.playlist.domain.Playlist;
import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistCreateRequest;
import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistDto;
import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistMapper;
import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistUpdateRequest;
import org.codeit.sb06.team03.mopl.user.infra.in.UserDto;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BasicBffPlaylistService implements BffPlaylistService {

    private final PlaylistMapper playlistMapper;
    private final CreatePlaylistUseCase createPlaylistUseCase;
    private final GetPlaylistUseCase getPlaylistUseCase;
    private final UpdatePlaylistUseCase updatePlaylistUseCase;
    private final DeletePlaylistUseCase deletePlaylistUseCase;

    @Override
    public PlaylistDto createPlaylist(PlaylistCreateRequest request, UUID ownerId) {

        CreatePlaylistCommand command = playlistMapper.toCommand(request);
        Playlist playlist = createPlaylistUseCase.create(command, ownerId);

        UUID id = playlist.getId();
        UserDto owner = null; // TODO
        String title = playlist.getTitle();
        String description = playlist.getDescription();
        Instant updatedAt = playlist.getUpdatedAt();
        long subscriberCount = 0; // TODO
        boolean subscribed = false; // TODO
        // List<ContentDto> contents; // TODO

        return new PlaylistDto(
                id,
                null,
                title,
                description,
                updatedAt,
                subscriberCount,
                subscribed
                // null
        );
    }

    @Override
    public PlaylistDto getPlaylist(String playlistId, UUID ownerId) {
        return getPlaylistUseCase.get(playlistId, ownerId);
    }

    @Override
    public PlaylistDto updatePlayList(String playlistId, PlaylistUpdateRequest request, UUID ownerId) {

        UpdatePlaylistCommand command = playlistMapper.toCommand(request);
        Playlist playlist = updatePlaylistUseCase.update(playlistId, command, ownerId);

        UUID id = playlist.getId();
        UserDto owner = null; // TODO
        String title = playlist.getTitle();
        String description = playlist.getDescription();
        Instant updatedAt = playlist.getUpdatedAt();
        long subscriberCount = 0; // TODO
        boolean subscribed = false; // TODO
        // List<ContentDto> contents; // TODO
        return new PlaylistDto(
                id,
                owner,
                title,
                description,
                updatedAt,
                subscriberCount,
                subscribed
                // null
        );
    }

    @Override
    public void deletePlaylist(String playlistId, UUID ownerId) {
        deletePlaylistUseCase.delete(playlistId, ownerId);
    }
}
