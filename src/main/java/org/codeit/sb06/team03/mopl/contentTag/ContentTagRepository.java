package org.codeit.sb06.team03.mopl.contentTag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ContentTagRepository extends JpaRepository<ContentTag, ContentTagId> {

    @Query("select ct from ContentTag ct join fetch ct.tag where ct.id.contentId = :contentId")
    List<ContentTag> findByContentId(@Param("contentId") UUID contentId);
}
