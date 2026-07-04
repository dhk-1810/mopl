package org.codeit.sb06.team03.mopl.account.application.in;

import org.codeit.sb06.team03.mopl.account.domain.Account;
import org.codeit.sb06.team03.mopl.profile.infra.in.CursorRequestUserDto;
import org.springframework.data.domain.Slice;

import java.util.Optional;
import java.util.UUID;

public interface GetAccountUseCase {

    Slice<Account> getById(CursorRequestUserDto request);

    Account getById(UUID accountId);

    Optional<Account> getByEmail(String email);

    Long count(CursorRequestUserDto request);
}
