package org.codeit.sb06.team03.mopl.content.infra.out;

import org.codeit.sb06.team03.mopl.content.domain.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
}
