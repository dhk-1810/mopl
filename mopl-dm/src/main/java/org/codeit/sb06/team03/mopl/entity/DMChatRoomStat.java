package org.codeit.sb06.team03.mopl.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "dm_chat_room_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"dm_chat_room_id", "account_id"})
)
@SQLDelete(sql = "UPDATE dm_chat_room_stats SET is_deleted = true WHERE id = ? AND version = ?")
@SQLRestriction("is_deleted = false")
public class DMChatRoomStat {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dm_chat_room_id", nullable = false)
    private DMChatRoom dmChatRoom;

    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private short version;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @NotNull
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @NotNull
    @Column(name = "activity", nullable = false)
    private boolean activity;

    @NotNull
    @Column(name = "has_unread", nullable = false)
    private boolean hasUnread;

    public static DMChatRoomStat create(DMChatRoom dmChatRoom, UUID accountId) {
        var dmChatRoomStat = new DMChatRoomStat();
        dmChatRoomStat.id = UUID.randomUUID();
        dmChatRoomStat.dmChatRoom = dmChatRoom;
        dmChatRoomStat.updatedAt = Instant.now();
        dmChatRoomStat.accountId = accountId;
        dmChatRoomStat.activity = false;
        dmChatRoomStat.hasUnread = false;
        return dmChatRoomStat;
    }

    public void updateActivity(boolean activity) {
        this.activity = activity;
        this.updatedAt = Instant.now();
    }

    public void markAsRead() {
        this.hasUnread = false;
        this.updatedAt = Instant.now();
    }

    public void markAsUnread() {
        this.hasUnread = true;
        this.updatedAt = Instant.now();
    }
}
