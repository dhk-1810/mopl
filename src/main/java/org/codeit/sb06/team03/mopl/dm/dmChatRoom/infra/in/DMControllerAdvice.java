package org.codeit.sb06.team03.mopl.dm.dmChatRoom.infra.in;

import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.exception.DMChatRoomAlreadyExistsException;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.exception.DMChatRoomNotFoundException;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.exception.DMMessageNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "org.codeit.sb06.team03.mopl.dm")
public class DMControllerAdvice {

    @ExceptionHandler(DMChatRoomNotFoundException.class)
    public ResponseEntity<String> handleDMChatRoomNotFound(DMChatRoomNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(DMMessageNotFoundException.class)
    public ResponseEntity<String> handleDMMessageNotFound(DMMessageNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(DMChatRoomAlreadyExistsException.class)
    public ResponseEntity<String> handleConflict(DMChatRoomAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
