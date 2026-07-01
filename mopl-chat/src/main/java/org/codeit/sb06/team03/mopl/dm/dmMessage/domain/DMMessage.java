package org.codeit.sb06.team03.mopl.dm.dmMessage.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummary;
import org.codeit.sb06.team03.mopl.dm.dmMessage.domain.event.DMMessageEvent;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE dm_messages SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class DMMessage extends AbstractAggregateRoot<DMMessage> {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "dmChatRoom_id", nullable = false)
    private UUID dmChatRoomId;

    @NotNull
    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @NotNull
    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;

    @NotNull
    @Column(name = "content", length = 1_000, nullable = false)
    private String content;

    @NotNull
    @Column(name = "has_unread", nullable = false)
    private boolean hasUnread;

    public static DMMessage create(UUID dmChatRoomId, UUID senderId, UUID receiverId, String content, UserSummary sender, UserSummary receiver) {
        var dmMessage = new DMMessage();
        dmMessage.id = UUID.randomUUID();
        dmMessage.createdAt = Instant.now();
        dmMessage.dmChatRoomId = dmChatRoomId;
        dmMessage.senderId = senderId;
        dmMessage.receiverId = receiverId;
        dmMessage.content = content;
        dmMessage.hasUnread = true;
        dmMessage.registerEvent(new DMMessageEvent.MessageSentEvent(dmMessage.id, dmChatRoomId, senderId, receiverId, content, dmMessage.createdAt, sender, receiver));
        return dmMessage;
    }

    public void markAsRead() {
        this.hasUnread = false;
    }

    public void receive() {
        this.registerEvent(new DMMessageEvent.MessageReceivedEvent(this.id, this.dmChatRoomId, this.senderId, this.receiverId));
    }

    public void pass() {
        this.registerEvent(new DMMessageEvent.MessagePassedEvent(this.id, this.dmChatRoomId, this.receiverId, this.content));
    }
}


