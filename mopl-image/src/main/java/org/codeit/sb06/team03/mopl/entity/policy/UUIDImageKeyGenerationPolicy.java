package org.codeit.sb06.team03.mopl.entity.policy;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("imageUUIDImageKeyGenerationPolicy")
public class UUIDImageKeyGenerationPolicy implements ImageKeyGenerationPolicy {
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
