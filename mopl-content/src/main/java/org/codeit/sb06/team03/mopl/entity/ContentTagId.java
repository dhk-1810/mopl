package org.codeit.sb06.team03.mopl.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ContentTagId {

    @Column(name = "content_id")
    private UUID contentId;

    @Column(name = "tag_id")
    private UUID tagId;
}
