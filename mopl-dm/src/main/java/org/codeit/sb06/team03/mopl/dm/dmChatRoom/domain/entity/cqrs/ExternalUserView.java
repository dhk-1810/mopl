package org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.entity.cqrs;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "external_user_views")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExternalUserView {

    @Id
    @Column(name = "user_id")
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String profileImageKey;

    public static ExternalUserView create(UUID id, String name, String profileImageKey) {
        ExternalUserView view = new ExternalUserView();
        view.id = id;
        view.name = name;
        view.profileImageKey = profileImageKey;
        return view;
    }

    public void update(String name, String profileImageKey) {
        this.name = name;
        this.profileImageKey = profileImageKey;
    }
}
