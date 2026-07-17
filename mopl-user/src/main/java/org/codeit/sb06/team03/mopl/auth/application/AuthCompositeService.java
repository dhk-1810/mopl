package org.codeit.sb06.team03.mopl.auth.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.application.AccountCommandService;
import org.codeit.sb06.team03.mopl.account.application.AccountQueryService;
import org.codeit.sb06.team03.mopl.account.application.in.ResetPasswordCommand;
import org.codeit.sb06.team03.mopl.account.domain.Account;
import org.codeit.sb06.team03.mopl.auth.infra.in.AuthMapper;
import org.codeit.sb06.team03.mopl.auth.infra.in.ResetPasswordRequest;
import org.codeit.sb06.team03.mopl.profile.infra.in.UserDto;
import org.codeit.sb06.team03.mopl.image.application.ImageQueryService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthCompositeService {

    private final AuthMapper authMapper;
    private final AccountCommandService accountCommandService;
    private final AccountQueryService accountQueryService;
    private final ImageQueryService imageQueryService;

    public void resetPassword(ResetPasswordRequest request) {
        ResetPasswordCommand command = authMapper.toCommand(request);
        accountCommandService.resetPassword(command);
    }

    public UserDto getUserDto(UUID accountId) {
        Account account = accountQueryService.getById(accountId);
        String presignedUrl = imageQueryService.getPresignedUrl(account.getProfile().getImageKey());
        return UserDto.from(account, account.getProfile(), presignedUrl);
    }
}
