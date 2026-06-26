package org.codeit.sb06.team03.mopl.tag.entity;

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
import jakarta.persistence.Column;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "tags")
@SQLDelete(sql = "UPDATE tags SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Tag {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

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
}
