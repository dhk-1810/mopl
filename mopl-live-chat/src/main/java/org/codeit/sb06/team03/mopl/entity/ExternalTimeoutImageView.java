package org.codeit.sb06.team03.mopl.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Setter
@Getter
@ToString
public class ExternalTimeoutImageView {

    private UUID id;
    private boolean isDeleted = false;
    private String key;
    private Instant exp;
    private String presignedUrl;

    public static ExternalTimeoutImageView create(String key, Instant exp, String presignedUrl) {
        var timeoutImage = new ExternalTimeoutImageView();
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
