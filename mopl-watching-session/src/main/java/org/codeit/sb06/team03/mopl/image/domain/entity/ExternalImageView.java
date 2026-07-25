package org.codeit.sb06.team03.mopl.image.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ExternalImageView {

    private UUID id;
    private String imageKey;
    private String presignedUrl;
    private boolean isDeleted = false;
    private Instant exp;
}
