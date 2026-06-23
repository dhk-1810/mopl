package org.codeit.sb06.team03.mopl.content.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeit.sb06.team03.mopl.content.Content;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "contents_tags")
public class ContentTag {

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
