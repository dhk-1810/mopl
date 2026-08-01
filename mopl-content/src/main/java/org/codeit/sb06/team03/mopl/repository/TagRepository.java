package org.codeit.sb06.team03.mopl.repository;

import org.codeit.sb06.team03.mopl.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findByNameIn(Set<String> names);
}
