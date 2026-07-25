package org.codeit.sb06.team03.mopl.repository;

import io.github.openfeign.querydsl.jpa.spring.repository.QuerydslJpaRepository;
import org.codeit.sb06.team03.mopl.entity.Followee;

import java.util.UUID;

public interface JpaFollowRepository extends QuerydslJpaRepository<Followee, UUID> {
}
