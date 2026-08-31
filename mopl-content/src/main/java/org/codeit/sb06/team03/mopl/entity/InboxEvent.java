package org.codeit.sb06.team03.mopl.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "inbox_events", indexes = {
        @Index(name = "idx_inbox_message_id", columnList = "message_id", unique = true)
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InboxEvent {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "message_id", nullable = false, unique = true)
    private String messageId;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public static InboxEvent create(String messageId, String aggregateType, String eventType, String payload) {
        InboxEvent event = new InboxEvent();
        event.id = UUID.randomUUID();
        event.messageId = messageId;
        event.aggregateType = aggregateType;
        event.eventType = eventType;
        event.payload = payload;
        event.processedAt = Instant.now();
        return event;
    }
}
