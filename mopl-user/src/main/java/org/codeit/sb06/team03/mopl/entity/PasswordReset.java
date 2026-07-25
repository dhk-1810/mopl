package org.codeit.sb06.team03.mopl.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.vo.Password;
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
@Table(name = "password_resets")
@SQLDelete(sql = "UPDATE password_resets SET is_deleted = true WHERE account_id = ? AND version = ?")
@SQLRestriction("is_deleted = false")
public class PasswordReset {

    @Id
    @Column(name = "account_id", nullable = false)
    private UUID id;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private short version;

    @Embedded
    private Password tempPassword;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public boolean validateTempPassword(Password inputPassword) {
        return this.tempPassword.equals(inputPassword);
    }

    public static PasswordReset create(Account account, Password tempPassword, Instant expiresAt) {
        var passwordReset = new PasswordReset();
        passwordReset.account = account;
        passwordReset.tempPassword = tempPassword;
        passwordReset.expiresAt = expiresAt;
        return passwordReset;
    }
}
