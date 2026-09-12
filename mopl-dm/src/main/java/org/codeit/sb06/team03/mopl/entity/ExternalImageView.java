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
@SQLRestriction("is_deleted = false")
public class ExternalImageView {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "image_key", unique = true)
    private String imageKey;

    @Column(name = "presigned_url", length = 1024)
    private String presignedUrl;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "exp")
    private Instant exp;
}
