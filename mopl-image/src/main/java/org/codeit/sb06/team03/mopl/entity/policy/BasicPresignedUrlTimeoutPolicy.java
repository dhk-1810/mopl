package org.codeit.sb06.team03.mopl.entity.policy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component("imageBasicPresignedUrlTimeoutPolicy")
public class BasicPresignedUrlTimeoutPolicy implements PresignedUrlTimeoutPolicy {

    private final Duration timeout;

    public BasicPresignedUrlTimeoutPolicy(@Value("${mopl.storage.aws.signature-duration-in-hours}") long timeout) {
        this.timeout = Duration.ofHours(timeout);
    }

    @Override
    public Instant createExp(Instant now) {
        return now.plus(timeout);
    }

    @Override
    public Duration timeout() {
        return timeout;
    }
}
