package org.codeit.sb06.team03.mopl.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

public abstract sealed class DMEvent{

    @Getter
    @RequiredArgsConstructor
    public static final class DMSentEvent extends DMEvent{
        private final UUID receiverId;
        private final String senderName;
        private final String content;
    }


}
