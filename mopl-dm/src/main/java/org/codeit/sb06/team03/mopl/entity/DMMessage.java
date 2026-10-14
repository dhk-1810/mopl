package org.codeit.sb06.team03.mopl.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "dm_messages")
public class DMMessage {

    @Id
    private UUID id;

    private boolean isDeleted = false;

    @CreatedDate
    private Instant createdAt;

    private UUID dmChatRoomId;

    private UUID senderId;

    private UUID receiverId;

    private String content;

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
