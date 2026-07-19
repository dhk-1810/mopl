package org.codeit.sb06.team03.mopl.entity.policy;

import java.time.Duration;
import java.time.Instant;

public interface PresignedUrlTimeoutPolicy {
    Instant createExp(Instant now);
    Duration timeout();
}
