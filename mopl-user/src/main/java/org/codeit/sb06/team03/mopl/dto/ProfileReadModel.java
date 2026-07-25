package org.codeit.sb06.team03.mopl.dto;

import java.util.UUID;

public record ProfileReadModel(
        UUID userId,
        String name,
        String imageKey,
        String email,
        String role
) {}
