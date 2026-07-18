package org.codeit.sb06.team03.mopl.dto.response;

import org.codeit.sb06.team03.mopl.UserSummary;
import org.codeit.sb06.team03.mopl.content.controller.ContentDto;
import org.codeit.sb06.team03.mopl.playlist.config.PlaylistReadModel;
import org.codeit.sb06.team03.mopl.domain.entity.Playlist;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PlaylistDto(
        UUID id,
        UserSummary owner,
        String title,
        String description,
        Instant updatedAt,
        long subscriberCount,
        boolean subscribedByMe,
        List<ContentDto> contents
) {

    public static PlaylistDto toDto(Playlist playlist, UserSummary owner, boolean subscribedByMe, List<ContentDto> contents) {
        return new PlaylistDto(
                playlist.getId(),
                owner,
                playlist.getTitle(),
                playlist.getDescription(),
                playlist.getUpdatedAt(),
                playlist.getSubscriberCount(),
                subscribedByMe,
                contents
        );
    }

    public static PlaylistDto toDto(PlaylistReadModel readModel, UserSummary owner, boolean subscribedByMe, List<ContentDto> contents) {
        return new PlaylistDto(
                readModel.id(),
                owner,
                readModel.title(),
                readModel.description(),
                readModel.updatedAt(),
                readModel.subscriberCount(),
                subscribedByMe,
                contents
        );
    }
}


