package org.codeit.sb06.team03.mopl.content.infra.in;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ReviewUpdateRequest(

        String text,

        @Min(1) @Max(5)
        Double rating
) {}
