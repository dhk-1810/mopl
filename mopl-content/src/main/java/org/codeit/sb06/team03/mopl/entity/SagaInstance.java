package org.codeit.sb06.team03.mopl.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeit.sb06.team03.mopl.enums.ParticipantStatus;
import org.codeit.sb06.team03.mopl.enums.SagaStatus;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "saga_instances")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SagaInstance {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "content_id", nullable = false)
    private UUID contentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SagaStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "playlist_status", nullable = false)
    private ParticipantStatus playlistStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "watching_session_status", nullable = false)
    private ParticipantStatus watchingSessionStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static SagaInstance create(UUID sagaId, UUID contentId) {
        SagaInstance instance = new SagaInstance();
        instance.id = sagaId;
        instance.contentId = contentId;
        instance.status = SagaStatus.STARTED;
        instance.playlistStatus = ParticipantStatus.PENDING;
        instance.watchingSessionStatus = ParticipantStatus.PENDING;
        instance.createdAt = Instant.now();
        instance.updatedAt = Instant.now();
        return instance;
    }

    public void updateParticipant(String participant, String responseStatus) {
        ParticipantStatus pStatus = "SUCCESS".equalsIgnoreCase(responseStatus) ? ParticipantStatus.SUCCESS : ParticipantStatus.FAILED;
        if ("PLAYLIST".equalsIgnoreCase(participant)) {
            this.playlistStatus = pStatus;
        } else if ("WATCHING_SESSION".equalsIgnoreCase(participant)) {
            this.watchingSessionStatus = pStatus;
        }
        this.updatedAt = Instant.now();
    }

    public boolean isAllSuccess() {
        return this.playlistStatus == ParticipantStatus.SUCCESS && this.watchingSessionStatus == ParticipantStatus.SUCCESS;
    }

    public boolean isAnyFailed() {
        return this.playlistStatus == ParticipantStatus.FAILED || this.watchingSessionStatus == ParticipantStatus.FAILED;
    }

    public boolean isFinished() {
        return this.status == SagaStatus.COMPLETED || this.status == SagaStatus.FAILED || this.status == SagaStatus.TIMED_OUT;
    }

    public void markCompleted() {
        this.status = SagaStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void markFailed() {
        this.status = SagaStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    public void markTimedOut() {
        this.status = SagaStatus.TIMED_OUT;
        this.updatedAt = Instant.now();
    }
}
