package org.codeit.sb06.team03.mopl.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "level", nullable = false)
    private NotificationLevel level;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    private Notification(UUID receiverId, String title, String content, NotificationLevel level) {
        this.id = UUID.randomUUID();
        this.receiverId = receiverId;
        this.title = title;
        this.content = content;
        this.level = level;
        this.createdAt = Instant.now();
    }

    public static Notification create(UUID receiverId, String title, String content, NotificationLevel level){
        return new Notification(receiverId, title, content, level);
    }
}
