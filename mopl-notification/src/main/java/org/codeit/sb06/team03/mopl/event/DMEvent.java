package org.codeit.sb06.team03.mopl.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeit.sb06.team03.mopl.dto.response.DirectMessageDto;

import java.util.UUID;

public abstract class DMEvent {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class NewMessageMarkEvent extends DMEvent {
        private UUID receiverId;
        private String senderName;
        private String content;
        private DirectMessageDto directMessage;
    }

}
