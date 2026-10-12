package org.codeit.sb06.team03.mopl.dto;

import org.codeit.sb06.team03.mopl.enums.ContentType;

import java.util.List;
import java.util.UUID;

public record PlaylistCurationBackupDto(
        List<CurationItem> curations,
        ExternalContentViewItem contentView
) {
    public record CurationItem(
            UUID playlistId,
            UUID contentId,
            String contentTitle
    ) {}

    public record ExternalContentViewItem(
            UUID id,
            ContentType type,
            String title,
            String description,
            String thumbnailKey,
            String tags,
            double averageRating,
            long reviewCount,
            long watcherCount
    ) {}
}
