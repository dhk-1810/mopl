package org.codeit.sb06.team03.mopl.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@Entity
@Table(name = "timeout_images")
@SQLDelete(sql = "UPDATE timeout_images SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class TimeoutImage {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "image_key", unique = true)
    private String key;

    @Column(name = "exp")
    private Instant exp;

    @Column(name = "presigned_url", length = 1024)
    private String presignedUrl;

    public static TimeoutImage create(String key, Instant exp, String presignedUrl) {
        var timeoutImage = new TimeoutImage();
        timeoutImage.id = UUID.randomUUID();
        timeoutImage.key = key;
        timeoutImage.exp = exp;
        timeoutImage.presignedUrl = presignedUrl;
        return timeoutImage;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(exp);
    }

    public void updatePresignedUrl(Instant exp, String presignedUrl) {
        this.exp = exp;
        this.presignedUrl = presignedUrl;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
