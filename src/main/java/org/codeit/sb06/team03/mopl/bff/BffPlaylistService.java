package org.codeit.sb06.team03.mopl.bff;

import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistCreateRequest;
import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistDto;
import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistUpdateRequest;

import java.util.UUID;

public interface BffPlaylistService {

    PlaylistDto createPlaylist(PlaylistCreateRequest request, UUID ownerId);

    PlaylistDto getPlaylist(String playlistId, UUID ownerId);

    PlaylistDto updatePlayList(String playlistId, PlaylistUpdateRequest request, UUID ownerId);

    void deletePlaylist(String playlistId, UUID ownerId);
}
