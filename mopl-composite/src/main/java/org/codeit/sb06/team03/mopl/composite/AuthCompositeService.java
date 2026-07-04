package org.codeit.sb06.team03.mopl.composite;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.application.in.GetAccountUseCase;
import org.codeit.sb06.team03.mopl.account.application.in.ResetPasswordCommand;
import org.codeit.sb06.team03.mopl.account.application.in.ResetPasswordUseCase;
import org.codeit.sb06.team03.mopl.account.domain.Account;
import org.codeit.sb06.team03.mopl.auth.infra.in.AuthMapper;
import org.codeit.sb06.team03.mopl.auth.infra.in.ResetPasswordRequest;
import org.codeit.sb06.team03.mopl.profile.infra.in.UserDto;
import org.codeit.sb06.team03.mopl.image.application.in.GetPresignedUrlUseCase;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthCompositeService {

    private final AuthMapper authMapper;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final GetPresignedUrlUseCase getPresignedUrlUseCase;

    public void resetPassword(ResetPasswordRequest request) {
        ResetPasswordCommand command = authMapper.toCommand(request);
        resetPasswordUseCase.resetPassword(command);
    }

    public UserDto getUserDto(UUID accountId) {
        Account account = getAccountUseCase.getById(accountId);
        String presignedUrl = getPresignedUrlUseCase.getPresignedUrl(account.getProfile().getImageKey());
        return UserDto.from(account, account.getProfile(), presignedUrl);
    }
}
