package org.codeit.sb06.team03.mopl.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "tags")
@SQLDelete(sql = "UPDATE tags SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Tag {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    private Tag(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    public static Tag create(String name) {
        return new Tag(name);
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
