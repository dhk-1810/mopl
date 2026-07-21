package org.codeit.sb06.team03.mopl.exception;

public class InvalidIdentifierException extends NotificationException {
    public InvalidIdentifierException(String id) {
        super("Invalid identifier : %s".formatted(id));
    }
}
