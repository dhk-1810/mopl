package org.codeit.sb06.team03.mopl.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeit.sb06.team03.mopl.event.ProfileUpdatedEvent;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.lang.Nullable;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "profiles")
@SQLDelete(sql = "UPDATE profiles SET deleted_at = NOW() WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
public class Profile extends AbstractAggregateRoot<Profile> {

    @Id
    @Column(name = "id")
    private UUID accountId;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    @Column(name = "version")
    private short version;

    @NotNull
    @Column(name = "name")
    private String name;

    @Column(name = "image_key")
    private String imageKey;

    @MapsId("accountId")
    @JoinColumn(name = "id")
    @OneToOne
    private Account account;

    public static Profile create(UUID accountId, String name) {
        Profile profile = new Profile();
        profile.accountId = accountId;
        profile.name = name;
        profile.version = 0;
        return profile;
    }

    public Profile update(String name, @Nullable String imageKey) {
        this.name = name;
        if (imageKey != null && !imageKey.isBlank()) {
            this.imageKey = imageKey;
        }
        super.registerEvent(new ProfileUpdatedEvent(this.accountId, this.name, this.imageKey));
        return this;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}


