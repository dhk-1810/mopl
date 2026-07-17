package org.codeit.sb06.team03.mopl.follow.service;

import org.codeit.sb06.team03.mopl.follow.service.*;
import org.codeit.sb06.team03.mopl.follow.domain.FollowService;
import org.codeit.sb06.team03.mopl.follow.domain.Followee;
import org.codeit.sb06.team03.mopl.follow.exception.FolloweeNotFoundException;
import org.codeit.sb06.team03.mopl.follow.controller.FollowDto;
import org.codeit.sb06.team03.mopl.follow.repository.JpaFollowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class FollowCommandService {

    private final FollowService service;
    private final JpaFollowRepository jpaFollowRepository;

    public FollowCommandService(
            FollowService service,
            JpaFollowRepository jpaFollowRepository
    ) {
        this.service = service;
        this.jpaFollowRepository = jpaFollowRepository;
    }

    public Followee create(CreateFollowCommand command) {
        UUID id = command.accountId();
        Followee followee = service.create(id);
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
    public boolean followedByMe(FollowQuery query) {
        UUID followeeId = query.followeeId();
        UUID followerId = query.followerId();
        Followee followee = jpaFollowRepository.findById(followeeId)
                .orElseThrow(() -> new FolloweeNotFoundException(followeeId));
        return followee.isFollowedBy(followerId);
    }

    public FollowDto follow(FollowCommand command) {
        UUID followeeId = command.followeeId();
        UUID followerId = command.followerId();

        Followee followee = jpaFollowRepository.findById(followeeId)
                .orElseThrow(() -> new FolloweeNotFoundException(followeeId));
        Followee followed = service.follow(followee, followerId);
        Followee saved = jpaFollowRepository.save(followed);
        return new FollowDto(saved.getId(), followeeId, followerId);
    }

    public void unfollow(UnfollowCommand command) {
        UUID followerId = command.followerId();
        UUID unfollowId = command.unfollowId();

        Followee followee = jpaFollowRepository.findById(unfollowId)
                .orElseThrow(() -> new FolloweeNotFoundException(unfollowId));
        Followee followed = service.unfollow(followee, followerId);
        jpaFollowRepository.save(followed);
    }
}
