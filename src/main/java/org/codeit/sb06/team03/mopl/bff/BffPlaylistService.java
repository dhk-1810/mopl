package org.codeit.sb06.team03.mopl.bff;

import org.codeit.sb06.team03.mopl.playlist.infra.in.*;

import java.util.UUID;

public interface BffPlaylistService {

    PlaylistDto createPlaylist(PlaylistCreateRequest request, UUID ownerId);

    CursorResponsePlaylistDto getPlaylists(CursorRequestPlaylistDto request);

    PlaylistDto getPlaylist(String playlistId, UUID viewerId);

    PlaylistDto updatePlayList(String playlistId, PlaylistUpdateRequest request, UUID ownerId);

    void deletePlaylist(String playlistId, UUID ownerId);
}
