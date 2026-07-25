package org.codeit.sb06.team03.mopl.profile.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ExternalProfileView {

    private UUID accountId;
    private String name;
    private String imageKey;

    public static ExternalProfileView create(UUID accountId, String name, String imageKey) {
        ExternalProfileView profile = new ExternalProfileView();
        profile.accountId = accountId;
        profile.name = name;
        profile.imageKey = imageKey;
        return profile;
    }
}
