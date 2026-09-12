package org.codeit.sb06.team03.mopl.event;

public record PasswordResetedEvent(
        String emailAddress,
        String rawTempPassword,
        String expiresAt
) {}
