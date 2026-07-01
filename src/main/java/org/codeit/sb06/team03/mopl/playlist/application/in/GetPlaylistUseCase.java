package org.codeit.sb06.team03.mopl.playlist.application.in;

import org.codeit.sb06.team03.mopl.playlist.PlaylistReadModel;
import org.codeit.sb06.team03.mopl.playlist.infra.in.request.CursorRequestPlaylistDto;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface GetPlaylistUseCase {

    Slice<PlaylistReadModel> get(CursorRequestPlaylistDto request, UUID viewerId);

    PlaylistReadModel get(UUID playlistId, UUID viewerId);
}
