package org.codeit.sb06.team03.mopl.playlist.domain.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import jakarta.persistence.Column;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "curations")
@SQLDelete(sql = "UPDATE curations SET is_deleted = true WHERE playlist_id = ? AND content_id = ?")
@SQLRestriction("is_deleted = false")
public class Curation {

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

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
