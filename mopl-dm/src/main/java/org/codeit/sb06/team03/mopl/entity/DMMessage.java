package org.codeit.sb06.team03.mopl.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "dm_messages")
@SQLDelete(sql = "UPDATE dm_messages SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class DMMessage {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "dm_chat_room_id", nullable = false)
    private UUID dmChatRoomId;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "has_unread", nullable = false)
    private boolean hasUnread;

    public static DMMessage create(UUID dmChatRoomId, UUID senderId, UUID receiverId, String content) {
        var dmMessage = new DMMessage();
        dmMessage.id = UUID.randomUUID();
        dmMessage.createdAt = Instant.now();
        dmMessage.dmChatRoomId = dmChatRoomId;
        dmMessage.senderId = senderId;
        dmMessage.receiverId = receiverId;
        dmMessage.content = content;
        dmMessage.hasUnread = true;
        return dmMessage;
    }

    public void markAsRead() {
        this.hasUnread = false;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
