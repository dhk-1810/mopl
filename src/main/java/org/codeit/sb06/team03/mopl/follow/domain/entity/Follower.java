package org.codeit.sb06.team03.mopl.follow.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codeit.sb06.team03.mopl.follow.domain.Followee;

import java.util.Objects;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "followers")
@SQLDelete(sql = "UPDATE followers SET is_deleted = true WHERE followee_id = ? AND follower_id = ?")
@SQLRestriction("is_deleted = false")
public class Follower {

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @EmbeddedId
    private FollowerId id;

    @MapsId("followeeId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "followee_id", nullable = false)
    private Followee followee;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Follower follower = (Follower) o;
        return Objects.equals(id, follower.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
