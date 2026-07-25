package org.codeit.sb06.team03.mopl.entity.policy;

import java.time.Instant;

public interface TempPasswordResetTimeoutPolicy {
    Instant createExpiresAt();
}
