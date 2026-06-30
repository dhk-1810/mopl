package org.codeit.sb06.team03.mopl.liveChatRoom.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "live_chat_rooms",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "_id_live_chat_id",
                        columnNames = {"watcher_id", "live_chat_id"} // 한 유저가 같은 liveCHat에 참여 불가하게 제한.
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE live_chat_rooms SET is_deleted = true WHERE content_id = ?") // delete 대신 플래그만 세움.
@SQLRestriction("is_deleted = false") // WHERE is_deleted = false와 같은 역할.
public class LiveChatRoom extends AbstractAggregateRoot<LiveChatRoom> {

    @Id
    @Column(name = "content_id")
    private UUID contentId;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private short version;

    public static LiveChatRoom create(UUID contentId) {
        LiveChatRoom liveChatRoom = new LiveChatRoom();
        liveChatRoom.contentId = contentId;
        liveChatRoom.createdAt = Instant.now();
        return liveChatRoom;
    }
}
