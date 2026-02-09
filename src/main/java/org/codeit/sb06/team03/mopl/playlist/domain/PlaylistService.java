package org.codeit.sb06.team03.mopl.playlist.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PlaylistService {

    public Playlist create(String title, String description, UUID ownerId) {
        return Playlist.create(title, description, ownerId);
    }

}
