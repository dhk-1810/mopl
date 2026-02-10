package org.codeit.sb06.team03.mopl.playlist.application;

import org.codeit.sb06.team03.mopl.account.application.in.GetAccountUseCase;
import org.codeit.sb06.team03.mopl.user.infra.in.CursorRequestUserDto;
import org.codeit.sb06.team03.mopl.user.infra.in.CursorResponseUserDto;
import org.codeit.sb06.team03.mopl.user.infra.in.UserDto;

public class PlaylistQueryService implements GetAccountUseCase {

    @Override
    public CursorResponseUserDto get(CursorRequestUserDto request) {
        return null;
    }

    @Override
    public UserDto get(String accountId) {
        return null;
    }
}
