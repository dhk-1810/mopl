package org.codeit.sb06.team03.mopl.exception;

import org.codeit.sb06.team03.mopl.ErrorResponse;
import org.codeit.sb06.team03.mopl.exception.LiveChatRoomDuplicateException;
import org.codeit.sb06.team03.mopl.exception.LiveChatRoomNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class LiveChatRoomControllerAdvice {

    @ExceptionHandler(value = LiveChatRoomNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLiveChatRoomNotFoundException(LiveChatRoomNotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getClass().getSimpleName(), e.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(value = LiveChatRoomDuplicateException.class)
    public ResponseEntity<ErrorResponse> handleLiveChatRoomDuplicateException(LiveChatRoomDuplicateException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getClass().getSimpleName(), e.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}
