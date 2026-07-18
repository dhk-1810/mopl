package org.codeit.sb06.team03.mopl.domain.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "curations")
public class Curation {



    @EmbeddedId
    private CurationId id;

    private String contentTitle;

    private Instant createdAt;

    private Curation(CurationId id, String contentTitle) {
        this.id = id;
        this.createdAt = Instant.now();
        this.contentTitle = contentTitle;
    }

    public static Curation create(UUID playlistId, UUID contentId, String contentTitle){
        return new Curation(new CurationId(playlistId, contentId), contentTitle);
    }
}
