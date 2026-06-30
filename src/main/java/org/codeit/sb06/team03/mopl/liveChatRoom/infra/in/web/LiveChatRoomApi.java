package org.codeit.sb06.team03.mopl.liveChatRoom.infra.in.web;

import org.hibernate.validator.constraints.UUID;

import java.security.Principal;

public interface LiveChatRoomApi {

    void pubMessage(
            @UUID(message = "잘못된 UUID 형식입니다.") String contentId,
            LiveChatRoomSendRequest request,
            Principal principal
    );
}