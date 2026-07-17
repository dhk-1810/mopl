package org.codeit.sb06.team03.mopl.account.exception;

import java.util.UUID;

public class PasswordResetNotFound extends RuntimeException {

    public PasswordResetNotFound(UUID id) {
        super("Account not found: %s".formatted(id));
    }

}
