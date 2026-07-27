package org.codeit.sb06.team03.mopl.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContentBatchInfoEvent {
    private List<ContentInfoDto> contents;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentInfoDto {
        private UUID contentId;
        private String type;
        private String title;
        private String description;
        private String thumbnailKey;
        private Set<String> tags;
        private double averageRating;
        private long reviewCount;
        private long watcherCount;
    }
}
