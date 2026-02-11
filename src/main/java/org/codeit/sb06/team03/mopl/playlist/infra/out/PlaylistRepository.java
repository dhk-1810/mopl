package org.codeit.sb06.team03.mopl.playlist.infra.out;

import org.codeit.sb06.team03.mopl.playlist.domain.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlaylistRepository extends JpaRepository<Playlist, UUID> {

}
