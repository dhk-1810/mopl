package org.codeit.sb06.team03.mopl.account.application.in;

import org.codeit.sb06.team03.mopl.profile.infra.in.CursorRequestUserDto;
import org.codeit.sb06.team03.mopl.profile.infra.in.CursorResponseUserDto;
import org.codeit.sb06.team03.mopl.profile.infra.in.UserDto;

import java.util.UUID;

public interface GetAccountUseCase {

    CursorResponseUserDto get(CursorRequestUserDto request);

    UserDto get(UUID accountId);
}
