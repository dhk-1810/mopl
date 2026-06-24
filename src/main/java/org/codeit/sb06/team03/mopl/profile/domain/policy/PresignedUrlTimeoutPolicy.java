package org.codeit.sb06.team03.mopl.profile.domain.policy;

import java.time.Duration;
import java.time.Instant;

public interface PresignedUrlTimeoutPolicy {

    Instant createExp(Instant now);

    Duration timeout();
}
