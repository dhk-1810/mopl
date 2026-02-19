package org.codeit.sb06.team03.mopl.playlist.application.in;

import org.codeit.sb06.team03.mopl.playlist.infra.in.request.CursorRequestPlaylistDto;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.CursorResponsePlaylistDto;

import java.util.UUID;

public interface GetPlaylistsUseCase {

    CursorResponsePlaylistDto get(CursorRequestPlaylistDto request, UUID viewerId);
}
