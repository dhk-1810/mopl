package org.codeit.sb06.team03.mopl.playlist.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "subscriptions")
@SQLDelete(sql = "UPDATE subscriptions SET is_deleted = true WHERE playlist_id = ? AND subscriber_id = ?")
@SQLRestriction("is_deleted = false")
public class Subscription {

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @EmbeddedId
    private SubscriptionId id;

    private Subscription(SubscriptionId id) {
        this.id = id;
    }

    public static Subscription create(UUID playlistId, UUID userId) {
        return new Subscription(new SubscriptionId(playlistId, userId));
    }
}
