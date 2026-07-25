package org.codeit.sb06.team03.mopl.security.jwt;

import org.codeit.sb06.team03.mopl.dto.response.UserDto;

public record JwtDto(
        UserDto userDto,
        String accessToken
) {
}
