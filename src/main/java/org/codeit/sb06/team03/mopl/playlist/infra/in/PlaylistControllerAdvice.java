package org.codeit.sb06.team03.mopl.playlist.infra.in;

import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.common.error.ErrorResponse;
import org.codeit.sb06.team03.mopl.playlist.domain.exception.PlaylistNotFoundException;
import org.codeit.sb06.team03.mopl.playlist.domain.exception.SubscriptionAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;

@Slf4j
@RestControllerAdvice(basePackageClasses = PlaylistController.class)
public class PlaylistControllerAdvice {

    @ExceptionHandler(PlaylistNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlaylistNotFoundException(PlaylistNotFoundException e) {
        log.error(e.getMessage());
        var errorResponse = new ErrorResponse(
                e.getClass().getSimpleName(),
                "Playlist를 찾을 수 없습니다.",
                Collections.emptyList()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(SubscriptionAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleSubscriptionAlreadyExistsException(SubscriptionAlreadyExistsException e) {
        log.error(e.getMessage());
        var errorResponse = new ErrorResponse(
                e.getClass().getSimpleName(),
                "이미 구독 중인 플레이리스트입니다.",
                Collections.emptyList()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }


}
