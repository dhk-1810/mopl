package org.codeit.sb06.team03.mopl.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class AccountEvent {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class RoleUpdatedEvent extends AccountEvent {
        private UUID accountId;
        private String role;
    }
}
