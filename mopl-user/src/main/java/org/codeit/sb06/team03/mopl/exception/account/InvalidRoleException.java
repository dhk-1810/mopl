package org.codeit.sb06.team03.mopl.exception.account;

public class InvalidRoleException extends AccountException {

    public InvalidRoleException(String role) {
        super(String.format("Invalid role: '{%s}'", role));
    }
}
