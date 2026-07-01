package org.codeit.sb06.team03.mopl.common.error;

public class InvalidIdentifierException extends RuntimeException {
    public InvalidIdentifierException(String id) {
        super("Invalid identifier : %s".formatted(id));
    }
}
