package org.codeit.sb06.team03.mopl.entity.cqrs;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "followers")
public class ExternalFollowView {

    @EmbeddedId
    private ExternalFollowId id;

    public ExternalFollowView(ExternalFollowId id) {
        this.id = id;
    }

    public static ExternalFollowView create(UUID followeeId, UUID followerId) {
        return new ExternalFollowView(new ExternalFollowId(followeeId, followerId));
    }

    @NoArgsConstructor
    @Getter
    @Embeddable
    public static class ExternalFollowId implements Serializable {
        private static final long serialVersionUID = 1L;

        @Column(name = "followee_id")
        private UUID followeeId;

        @Column(name = "follower_id")
        private UUID followerId;

        public ExternalFollowId(UUID followeeId, UUID followerId) {
            this.followeeId = followeeId;
            this.followerId = followerId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExternalFollowId that = (ExternalFollowId) o;
            return Objects.equals(followeeId, that.followeeId) && Objects.equals(followerId, that.followerId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(followeeId, followerId);
        }
    }
}
