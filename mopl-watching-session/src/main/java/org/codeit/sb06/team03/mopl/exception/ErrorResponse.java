package org.codeit.sb06.team03.mopl.exception;

import java.util.List;

public record ErrorResponse(
        String exceptionName,
        String message,
        List<String> details
) {
}
