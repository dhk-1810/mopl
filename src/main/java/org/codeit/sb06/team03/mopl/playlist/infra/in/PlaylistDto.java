package org.codeit.sb06.team03.mopl.playlist.infra.in;

import org.codeit.sb06.team03.mopl.user.infra.in.UserDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PlaylistDto(
        UUID id,
        UserDto owner, // TODO 확인필요
        String title,
        String description,
        Instant updatedAt,
        long subscriberCount,
        boolean subscribedByMe
        //List<ContentDto> contents // TODO
) {
}
