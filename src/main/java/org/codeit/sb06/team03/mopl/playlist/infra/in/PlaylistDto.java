package org.codeit.sb06.team03.mopl.playlist.infra.in;

import org.codeit.sb06.team03.mopl.playlist.domain.entity.Playlist;
import org.codeit.sb06.team03.mopl.user.infra.in.UserDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PlaylistDto(
        UUID id,
        UserSummaryDto owner,
        String title,
        String description,
        Instant updatedAt,
        long subscriberCount,
        boolean subscribedByMe
        //List<ContentDto> contents // TODO
) {

    public static PlaylistDto toDto(Playlist playlist, UserDto owner, boolean subscribedByMe/*, List<ContentDto> contents*/) {
        return new PlaylistDto(
                playlist.getId(),
                null,
                playlist.getTitle(),
                playlist.getDescription(),
                playlist.getUpdatedAt(),
                playlist.getSubscriberCount(),
                subscribedByMe
                // contents
        );
    }
}
