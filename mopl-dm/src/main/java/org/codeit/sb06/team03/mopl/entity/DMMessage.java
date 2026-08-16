package org.codeit.sb06.team03.mopl.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeit.sb06.team03.mopl.dto.UserSummary;
import org.codeit.sb06.team03.mopl.event.DMEvent;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Document(collection = "dm_messages")
@CompoundIndex(name = "chatroom_createdat_idx", def = "{'dmChatRoomId': 1, 'createdAt': -1, '_id': -1}") // 복합 인덱스
public class DMMessage extends AbstractAggregateRoot<DMMessage> {

    @Id
    private UUID id;

    @Field("is_deleted")
    private boolean isDeleted = false;

    @NotNull
    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @NotNull
    @Field("dm_chat_room_id")
    private UUID dmChatRoomId;

    @NotNull
    @Field("sender_id")
    private UUID senderId;

    @NotNull
    @Field("receiver_id")
    private UUID receiverId;

    @NotNull
    @Field("content")
    private String content;

    @NotNull
    @Field("has_unread")
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
        dmMessage.registerEvent(new DMEvent.MessageSentEvent(dmMessage.id, dmChatRoomId, senderId, receiverId, content, dmMessage.createdAt, sender, receiver));
        return dmMessage;
    }

    public void markAsRead() {
        this.hasUnread = false;
    }

    public void receive() {
        this.registerEvent(new DMEvent.MessageReceivedEvent(this.id, this.dmChatRoomId, this.senderId, this.receiverId));
    }

    public void pass() {
        this.registerEvent(new DMEvent.MessagePassedEvent(this.id, this.dmChatRoomId, this.receiverId, this.content));
    }

    public void delete() {
        this.isDeleted = true;
    }
}
