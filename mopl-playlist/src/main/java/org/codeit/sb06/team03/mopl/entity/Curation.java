package org.codeit.sb06.team03.mopl.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "curations")
@SQLDelete(sql = "UPDATE curations SET deleted_at = NOW() WHERE playlist_id = ? AND content_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Curation {

    @EmbeddedId
    private CurationId id;

    private String contentTitle;

    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    private Curation(CurationId id, String contentTitle) {
        this.id = id;
        this.createdAt = Instant.now();
        this.contentTitle = contentTitle;
        this.deletedAt = null;
    }

    public static Curation create(UUID playlistId, UUID contentId, String contentTitle){
        return new Curation(new CurationId(playlistId, contentId), contentTitle);
    }

    public void delete() {
        this.deletedAt = Instant.now();
    }

    public void restore() {
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
