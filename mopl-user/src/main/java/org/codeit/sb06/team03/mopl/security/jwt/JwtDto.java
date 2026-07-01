package org.codeit.sb06.team03.mopl.security.jwt;

import org.codeit.sb06.team03.mopl.profile.infra.in.UserDto;

public record JwtDto(
        UserDto userDto,
        String accessToken
) {
}
