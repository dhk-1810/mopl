package org.codeit.sb06.team03.mopl.repository;

import io.github.openfeign.querydsl.jpa.spring.repository.QuerydslJpaRepository;
import org.codeit.sb06.team03.mopl.entity.Followee;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface JpaFollowRepository extends QuerydslJpaRepository<Followee, UUID> {
    @Query("SELECT f.id.followerId FROM Follower f WHERE f.id.followeeId = :followeeId")
    Set<UUID> findFollowerIdsByFolloweeId(@Param("followeeId") UUID followeeId);
}
