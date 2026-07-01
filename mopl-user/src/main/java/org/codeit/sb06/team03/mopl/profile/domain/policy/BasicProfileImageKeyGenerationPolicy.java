package org.codeit.sb06.team03.mopl.profile.domain.policy;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BasicProfileImageKeyGenerationPolicy implements ImageKeyGenerationPolicy {
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
