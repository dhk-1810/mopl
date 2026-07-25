package org.codeit.sb06.team03.mopl.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.Followee;
import org.codeit.sb06.team03.mopl.exception.follow.FolloweeNotFoundException;
import org.codeit.sb06.team03.mopl.dto.response.FollowDto;
import org.codeit.sb06.team03.mopl.repository.JpaFollowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class FollowCommandService {

    private final JpaFollowRepository jpaFollowRepository;

    public Followee create(UUID accountId) {
        Followee followee = Followee.create(accountId);
        return jpaFollowRepository.save(followee);
    }

    @Transactional(readOnly = true)
    public long count(String followeeId) {
        final UUID followeeUUID = UUID.fromString(followeeId);
        return jpaFollowRepository.findById(followeeUUID)
                .map(Followee::getFollowerCount)
                .orElse(0L);
    }

    @Transactional(readOnly = true)
    public boolean followedByMe(UUID followeeId, UUID followerId) {
        Followee followee = jpaFollowRepository.findById(followeeId)
                .orElseThrow(() -> new FolloweeNotFoundException(followeeId));
        return followee.isFollowedBy(followerId);
    }

    public FollowDto follow(UUID followeeId, UUID followerId) {
        Followee followee = jpaFollowRepository.findById(followeeId)
                .orElseThrow(() -> new FolloweeNotFoundException(followeeId));
        followee.addFollower(followerId);
        Followee saved = jpaFollowRepository.save(followee);
        return new FollowDto(saved.getId(), followeeId, followerId);
    }

    public void unfollow(UUID followerId, UUID unfollowId) {
        Followee followee = jpaFollowRepository.findById(unfollowId)
                .orElseThrow(() -> new FolloweeNotFoundException(unfollowId));
        followee.removeFollower(followerId);
        jpaFollowRepository.save(followee);
    }
}
