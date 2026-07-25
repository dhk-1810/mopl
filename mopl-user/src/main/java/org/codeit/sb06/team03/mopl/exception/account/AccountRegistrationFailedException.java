package org.codeit.sb06.team03.mopl.exception.account;

public class AccountRegistrationFailedException extends AccountException {

    public AccountRegistrationFailedException(Throwable throwable) {
        super(throwable.getMessage());
    }
}
