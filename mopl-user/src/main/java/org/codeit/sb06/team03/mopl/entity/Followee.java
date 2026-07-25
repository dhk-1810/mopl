package org.codeit.sb06.team03.mopl.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.codeit.sb06.team03.mopl.event.FollowEvent.FollowedEvent;
import org.codeit.sb06.team03.mopl.event.FollowEvent.FolloweeCreatedEvent;
import org.springframework.data.domain.AbstractAggregateRoot;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.codeit.sb06.team03.mopl.event.FollowEvent.UnfollowedEvent;

@Getter
@Setter
@Entity
@Table(name = "followees")
@SQLDelete(sql = "UPDATE followees SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Followee extends AbstractAggregateRoot<Followee> {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "follower_count", nullable = false)
    private long followerCount;

    @OneToMany(mappedBy = "followee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Follower> followers = new HashSet<>();

    public static Followee create(UUID id) {
        var followee = new Followee();
        followee.id = id;
        followee.followerCount = 0;
        followee.registerEvent(new FolloweeCreatedEvent());
        return followee;
    }

    public void addFollower(UUID followerId) {
        if (id.equals(followerId)) {
            return;
        }
        if (isFollowedBy(followerId)) {
            return;
        }
        var follower = new Follower();
        follower.setId(new FollowerId(this.id, followerId));
        follower.setFollowee(this);
        followers.add(follower);
        followerCount++;
        super.registerEvent(new FollowedEvent(follower.getFollowee().getId(), followerId));
    }

    public void removeFollower(UUID followerId) {
        boolean removed = followers.removeIf(follower -> follower.getId().getFollowerId().equals(followerId));
        if (removed) {
            followerCount--;
            super.registerEvent(new UnfollowedEvent());
        }
    }

    public boolean isFollowedBy(UUID followerId) {
        return followers.stream().anyMatch(follower -> follower.getId().getFollowerId().equals(followerId));
    }
}
