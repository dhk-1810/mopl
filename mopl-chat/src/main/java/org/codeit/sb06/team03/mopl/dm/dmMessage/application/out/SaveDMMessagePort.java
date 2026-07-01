package org.codeit.sb06.team03.mopl.dm.dmMessage.application.out;

import org.codeit.sb06.team03.mopl.dm.dmMessage.domain.DMMessage;

import java.util.UUID;

public interface SaveDMMessagePort {

    DMMessage save(DMMessage dmMessage);

    void markAsRead(UUID messageId);
}