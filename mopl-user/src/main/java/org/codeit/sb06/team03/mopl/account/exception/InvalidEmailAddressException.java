package org.codeit.sb06.team03.mopl.account.exception;

public class InvalidEmailAddressException extends AccountException {
    
    public InvalidEmailAddressException(String emailAddress) {
        super("Invalid emailAddress format: '%s'".formatted(emailAddress));
    }
}
