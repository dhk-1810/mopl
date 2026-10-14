package org.codeit.sb06.team03.mopl.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "timeout_images")
@SQLRestriction("deleted_at IS NULL")
public class ExternalImageView {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "image_key", unique = true)
    private String imageKey;

    @Column(name = "presigned_url", length = 1024)
    private String presignedUrl;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "exp")
    private Instant exp;

    public static ExternalImageView create(String imageKey, String presignedUrl, Instant exp) {
        ExternalImageView view = new ExternalImageView();
        view.id = UUID.randomUUID();
        view.imageKey = imageKey;
        view.presignedUrl = presignedUrl;
        view.exp = exp;
        view.deletedAt = null;
        return view;
    }

    public void update(String presignedUrl, Instant exp) {
        this.presignedUrl = presignedUrl;
        this.exp = exp;
    }

    public boolean isExpired() {
        return exp != null && Instant.now().isAfter(exp);
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

}
