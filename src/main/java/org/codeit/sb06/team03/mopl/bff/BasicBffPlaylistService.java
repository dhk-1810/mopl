package org.codeit.sb06.team03.mopl.bff;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.playlist.application.in.CreatePlaylistCommand;
import org.codeit.sb06.team03.mopl.playlist.application.in.CreatePlaylistUseCase;
import org.codeit.sb06.team03.mopl.playlist.domain.Playlist;
import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistCreateRequest;
import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistDto;
import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistMapper;
import org.codeit.sb06.team03.mopl.user.infra.in.UserDto;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BasicBffPlaylistService implements BffPlaylistService {

    private final PlaylistMapper playlistMapper;
    private final CreatePlaylistUseCase createPlaylistUseCase;

    @Override
    public PlaylistDto createPlaylist(PlaylistCreateRequest request, UUID ownerId) {

        CreatePlaylistCommand command = playlistMapper.toCommand(request);
        Playlist playlist = createPlaylistUseCase.create(command, ownerId);

        // TODO 이벤트

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
}
