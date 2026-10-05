package org.codeit.sb06.team03.mopl.event;

public record PasswordResetEvent(
        String emailAddress,
        String rawTempPassword,
        String expiresAt
) {}
