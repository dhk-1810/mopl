package org.codeit.sb06.team03.mopl.playlist.infra.in;

import org.codeit.sb06.team03.mopl.playlist.application.in.CreatePlaylistCommand;
import org.springframework.stereotype.Component;

@Component
public class PlaylistMapper {

    public CreatePlaylistCommand toCommand(PlaylistCreateRequest request) {
        return new CreatePlaylistCommand(request.title(), request.description());
    }
}
