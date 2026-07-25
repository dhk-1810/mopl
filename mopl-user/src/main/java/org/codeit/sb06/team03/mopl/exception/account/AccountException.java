package org.codeit.sb06.team03.mopl.exception.account;

public abstract class AccountException extends RuntimeException {

    protected AccountException(String message) {
        super(message);
    }
}
