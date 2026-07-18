package org.codeit.sb06.team03.mopl.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "review_stats")
@SQLDelete(sql = "UPDATE review_stats SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class ReviewStats {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id", nullable = false)
    private Content content;

    @Column(name = "rating_sum", nullable = false)
    private long ratingSum;

    @Column(name = "review_count", nullable = false)
    private int reviewCount;

    @Column(name = "version", nullable = false)
    private short version;
}
