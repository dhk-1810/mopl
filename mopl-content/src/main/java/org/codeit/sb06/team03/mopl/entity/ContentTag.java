package org.codeit.sb06.team03.mopl.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "contents_tags")
@SQLDelete(sql = "UPDATE contents_tags SET is_deleted = true WHERE content_id = ? AND tag_id = ?")
@SQLRestriction("is_deleted = false")
public class ContentTag {

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @EmbeddedId
    private ContentTagId id;

    @MapsId("contentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    @MapsId("tagId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    private ContentTag(Content content, Tag tag) {
        this.id = new ContentTagId(content.getId(), tag.getId());
        this.content = content;
        this.tag = tag;
    }

    public static ContentTag create(Content content, Tag tag) {
        return new ContentTag(content, tag);
    }
}
