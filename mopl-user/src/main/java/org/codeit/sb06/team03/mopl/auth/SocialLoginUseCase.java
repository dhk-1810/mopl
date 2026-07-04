package org.codeit.sb06.team03.mopl.auth;

import org.codeit.sb06.team03.mopl.account.domain.Account;

public interface SocialLoginUseCase {

    Account loginOrRegister(SocialLoginCommand command);

}
