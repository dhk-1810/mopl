package org.codeit.sb06.team03.mopl.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.Followee;
import org.codeit.sb06.team03.mopl.repository.JpaFollowRepository;
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
