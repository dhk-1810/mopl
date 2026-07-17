package org.codeit.sb06.team03.mopl.follow.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.follow.domain.Followee;
import org.codeit.sb06.team03.mopl.follow.repository.JpaFollowRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FolloweeQueryService {

    private final JpaFollowRepository jpaFollowRepository;

    public Optional<Followee> findById(UUID id) {
        return jpaFollowRepository.findById(id);
    }
}
