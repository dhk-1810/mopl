package org.codeit.sb06.team03.mopl.event;

import org.codeit.sb06.team03.mopl.dto.response.DirectMessageDto;
import java.util.UUID;

public record NewMessageMarkEvent(
        UUID receiverId,
        String senderName,
        String content,
        DirectMessageDto directMessage
) {}
