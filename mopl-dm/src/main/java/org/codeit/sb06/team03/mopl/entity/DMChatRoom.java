package org.codeit.sb06.team03.mopl.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeit.sb06.team03.mopl.event.DMEvent;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "dm_chat_rooms")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE dm_chat_rooms SET is_deleted = true WHERE id = ? AND version = ?")
@SQLRestriction("is_deleted = false")
public class DMChatRoom extends AbstractAggregateRoot<DMChatRoom> {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private short version;

    @OneToMany(mappedBy = "dmChatRoom", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "accountId")
    private Map<UUID, DMChatRoomStat> dmChatRoomStats = new HashMap<>();

    public static DMChatRoom create(UUID userId, UUID withUserId) {
        var dmChatRoom = new DMChatRoom();
        dmChatRoom.id = UUID.randomUUID();
        dmChatRoom.addStat(withUserId);
        dmChatRoom.addStat(userId);
        dmChatRoom.registerEvent(new DMEvent.ChatRoomCreatedEvent(dmChatRoom.id, userId, withUserId));
        return dmChatRoom;
    }

    public void join(UUID userId) {
        DMChatRoomStat stat = this.dmChatRoomStats.get(userId);
        if (stat != null) {
            stat.updateActivity(true);
            registerEvent(new DMEvent.ChatRoomJoinedEvent(this.id, userId));
        }
    }

    public void leave(UUID userId) {
        DMChatRoomStat stat = this.dmChatRoomStats.get(userId);
        if (stat != null) {
            stat.updateActivity(false);
            registerEvent(new DMEvent.ChatRoomLeftEvent(this.id, userId));
        }
    }

    public void markAsRead(UUID userId) {
        DMChatRoomStat stat = this.dmChatRoomStats.get(userId);
        if (stat != null) {
            stat.markAsRead();
            registerEvent(new DMEvent.MessageReadEvent(this.id, userId));
        }
    }

    public void markAsUnread(UUID userId) {
        DMChatRoomStat stat = this.dmChatRoomStats.get(userId);
        if (stat != null) {
            stat.markAsUnread();
        }
    }

    public boolean isActive(UUID userId) {
        DMChatRoomStat stat = this.dmChatRoomStats.get(userId);
        return stat != null && stat.isActivity();
    }

    public UUID getOtherParticipant(UUID requesterId) {
        return this.dmChatRoomStats.keySet().stream()
                .filter(id -> !id.equals(requesterId))
                .findFirst()
                .orElseThrow();
    }

    private void addStat(UUID accountId) {
        this.dmChatRoomStats.put(accountId, DMChatRoomStat.create(this, accountId));
    }
}
