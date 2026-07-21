package org.codeit.sb06.team03.mopl.entity.cqrs;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "followers")
@SQLRestriction("is_deleted = false")
public class ExternalFollowView {

    @EmbeddedId
    private ExternalFollowId id;

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
    }
}
