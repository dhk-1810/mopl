package org.codeit.sb06.team03.mopl.profile.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "profiles")
public class ExternalProfileView {

    @Id
    @Column(name = "id")
    private UUID accountId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "image_key")
    private String imageKey;

    public static ExternalProfileView create(UUID accountId, String name, String imageKey) {
        ExternalProfileView profile = new ExternalProfileView();
        profile.accountId = accountId;
        profile.name = name;
        profile.imageKey = imageKey;
        return profile;
    }
}
