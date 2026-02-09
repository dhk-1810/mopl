package org.codeit.sb06.team03.mopl.playlist.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.playlist.application.in.CreatePlaylistCommand;
import org.codeit.sb06.team03.mopl.playlist.application.in.CreatePlaylistUseCase;
import org.codeit.sb06.team03.mopl.playlist.application.out.SavePlaylistPort;
import org.codeit.sb06.team03.mopl.playlist.domain.Playlist;
import org.codeit.sb06.team03.mopl.playlist.domain.PlaylistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PlaylistCommandService implements CreatePlaylistUseCase {

    private final SavePlaylistPort savePlaylistPort;
    private final PlaylistService playlistService;

    @Override
    @Transactional
    public Playlist create(CreatePlaylistCommand command, UUID ownerId) {

        final String title = command.title();
        final String description = command.description();

        Playlist newPlaylist = playlistService.create(title, description, ownerId);
        savePlaylistPort.save(newPlaylist);
        // TODO 팔로워에게 알림 발송
        return newPlaylist;
    }

}
