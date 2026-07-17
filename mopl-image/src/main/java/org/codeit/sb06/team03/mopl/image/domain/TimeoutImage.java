package org.codeit.sb06.team03.mopl.image.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.codeit.sb06.team03.mopl.image.service.PresignedUrlUpdateListener;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Setter
@Getter
@ToString
@Entity
@EntityListeners(PresignedUrlUpdateListener.class)
@Table(name = "timeout_images")
@SQLDelete(sql = "UPDATE timeout_images SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class TimeoutImage {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

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
}
